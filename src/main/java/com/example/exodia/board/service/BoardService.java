package com.example.exodia.board.service;

import com.example.exodia.board.domain.*;
import com.example.exodia.board.dto.BoardDetailDto;
import com.example.exodia.board.dto.BoardListResDto;
import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.dto.BoardUpdateDto;
import com.example.exodia.board.repository.BoardFileRepository;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.board.repository.BoardTagRepository;
import com.example.exodia.board.repository.BoardTagsRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentResDto;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.KafkaProducer;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final UploadAwsFileService uploadAwsFileService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BoardHitsService boardHitsService;
    private final BoardTagRepository boardTagRepository;
    private final BoardTagsRepository boardTagsRepository;
    private final KafkaProducer kafkaProducer;
    @Autowired
    public BoardService(BoardRepository boardRepository, UploadAwsFileService uploadAwsFileService,
                        BoardFileRepository boardFileRepository, UserRepository userRepository,
                        CommentRepository commentRepository, BoardHitsService boardHitsService,
                        BoardTagRepository boardTagRepository, BoardTagsRepository boardTagsRepository, KafkaProducer kafkaProducer) {
        this.boardRepository = boardRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.boardFileRepository = boardFileRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.boardHitsService = boardHitsService;
        this.boardTagRepository = boardTagRepository;
        this.boardTagsRepository = boardTagsRepository;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * 새로운 게시물을 생성하는 메서드
     * @param dto - 사용자가 작성한 게시물 정보 객체 (제목, 내용, 카테고리 등)
     * @param files - 사용자가 업로드한 파일 리스트
     * @param tagIds - 사용자가 추가한 태그 리스트
     * @return 생성된 게시물 객체
     */
    @Transactional
    public Board createBoard(BoardSaveReqDto dto, List<MultipartFile> files, List<Long> tagIds) {
        User user = validateUserAndCategory(dto.getUserNum(), dto.getCategory());
        Board board = dto.toEntity(user);
        board = boardRepository.save(board);

        // 태그와 연결 (BoardTag)
        if (tagIds != null && !tagIds.isEmpty()) {
            addTagsToBoard(board, tagIds);
        }

        processFiles(files, board);
        boardHitsService.resetBoardHits(board.getId());


        // Kafka 이벤트 전송

        // 공지사항 또는 경조사 게시물일 경우 모든 사용자에게 알림 전송
        String message = user.getDepartment().getName() + " 에서 " + dto.getTitle() + "를 작성했습니다";
        if (board.getCategory() == Category.NOTICE) {
            kafkaProducer.sendBoardEvent("notice-events", message); // 공지사항 토픽
        } else if (board.getCategory() == Category.FAMILY_EVENT) {
            kafkaProducer.sendBoardEvent("family-event-notices", message); // 경조사 토픽
        }



        return board;  // 기존 dto 대신 저장된 board 반환
    }


    /**
     * 태그를 게시판에 연결하는 메서드
     */
    private void addTagsToBoard(Board board, List<Long> tagIds) {
        // 태그 ID를 사용하여 태그 리스트를 조회합니다.
        List<BoardTags> tags = boardTagsRepository.findAllById(tagIds);

        // 각 태그와 게시물 간의 연결을 설정합니다.
        for (BoardTags tag : tags) {
            BoardTag boardTag = BoardTag.builder()
                    .board(board)
                    .boardTags(tag)
                    .build();
            boardTagRepository.save(boardTag);  // BoardTag 테이블에 저장
        }
    }

    /**
     * 파일 처리 로직
     */
    private void processFiles(List<MultipartFile> files, Board board) {
        List<MultipartFile> validFiles = files != null ? files.stream()
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toList()) : Collections.emptyList();

        if (!validFiles.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(validFiles, "board");

            if (validFiles.size() == s3FilePaths.size()) {
                for (int i = 0; i < validFiles.size(); i++) {
                    MultipartFile file = validFiles.get(i);
                    String s3FilePath = s3FilePaths.get(i);

                    BoardFile boardFile = BoardFile.createBoardFile(
                            board,
                            s3FilePath,
                            file.getContentType(),
                            file.getOriginalFilename(),
                            file.getSize()
                    );
                    boardFileRepository.save(boardFile);
                }
            } else {
                throw new IllegalStateException("파일 업로드에 실패하여, S3 경로와 파일 리스트의 크기가 일치하지 않습니다.");
            }
        }
    }

    /**
     * 사용자와 카테고리 유효성 검증 로직
     */
    private User validateUserAndCategory(String userNum, Category category) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        if ((category == Category.NOTICE || category == Category.FAMILY_EVENT) &&
                !user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 경조사 게시물은 인사팀만 작성할 수 있습니다.");
        }

        return user;
    }

    /**
     * 게시물 목록 조회
     */
    public Page<BoardListResDto> BoardListWithSearch(Pageable pageable, String searchType, String searchQuery, Category category, List<Long> tagIds) {
        Page<Board> boards;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    boards = boardRepository.findByTitleContainingIgnoreCaseAndCategoryAndDelYn(
                            searchQuery, category, DelYN.N, pageable);
                    break;
                case "content":
                    boards = boardRepository.findByContentContainingIgnoreCaseAndCategoryAndDelYn(
                            searchQuery, category, DelYN.N, pageable);
                    break;
                case "tags":
                    boards = boardRepository.findByTagsContainingIgnoreCaseAndCategoryAndDelYn(
                            searchQuery, category, DelYN.N, pageable);
                    break;
                case "title + content":
                    boards = boardRepository.findByTitleContainingOrContentContainingIgnoreCaseAndCategoryAndDelYn(
                            searchQuery, searchQuery, category, DelYN.N, pageable);
                    break;
                default:
                    boards = boardRepository.findByCategoryAndDelYn(category, DelYN.N, pageable);
                    break;
            }
        } else if (tagIds != null && !tagIds.isEmpty()) {
            boards = boardRepository.findByTagIdsAndCategoryAndDelYn(tagIds, category, DelYN.N, pageable);
        } else if (category != null) {
            boards = boardRepository.findByCategoryAndDelYn(category, DelYN.N, pageable);
        } else {
            boards = boardRepository.findAllWithPinnedByCategory(category, pageable);
        }

        return boards.map(Board::listFromEntity);
    }


    /**
     * 특정 게시물 상세 조회
     */
    public BoardDetailDto BoardDetail(Long id, String userNum) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        // 조회수 증가
        Long updatedHits = boardHitsService.incrementBoardHits(id, userNum);
        board.updateBoardHitsFromRedis(updatedHits);
        boardRepository.save(board);

        // 파일 및 댓글 조회
        List<BoardFile> boardFiles = boardFileRepository.findByBoardId(id);
        List<Comment> comments = commentRepository.findByBoardId(id);
        List<CommentResDto> commentResDto = comments.stream()
                .map(CommentResDto::fromEntity)
                .collect(Collectors.toList());

        // 태그 조회 (tagIds로 태그 이름 가져오기)
        List<BoardTags> tags = boardTagRepository.findByBoardId(id)
                .stream()
                .map(boardTag -> boardTag.getBoardTags())
                .collect(Collectors.toList());

        List<String> tagNames = tags.stream()
                .map(BoardTags::getTag)
                .collect(Collectors.toList());

        // BoardDetailDto 생성 및 설정
        BoardDetailDto boardDetailDto = board.detailFromEntity(boardFiles);
        boardDetailDto.setComments(commentResDto);
        boardDetailDto.setHits(updatedHits);
        boardDetailDto.setUser_num(board.getUser().getUserNum());
        boardDetailDto.setTags(tagNames);  // 태그 이름 설정

        return boardDetailDto;
    }


    /**
     * 게시물 업데이트 메서드
     */
    @Transactional
    public void updateBoard(Long id, BoardUpdateDto dto, List<MultipartFile> files, List<Long> tagIds, String userNum) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getUser().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        // 게시물 정보 업데이트
        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setCategory(dto.getCategory());
        board.setUpdatedAt(LocalDateTime.now());

        // 기존 태그 삭제 후 새로운 태그 추가
        boardTagRepository.deleteByBoardId(board.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            addTagsToBoard(board, tagIds);
        }

        // 파일 처리
        processFiles(files, board);

        boardRepository.save(board);
    }

    /**
     * 게시물 삭제 메서드
     */
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        // 게시글과 관련된 태그 삭제
        boardTagRepository.deleteByBoardId(id);

        boardRepository.delete(board);
    }

    /**
     * 게시물 상단 고정 메서드
     */
    @Transactional
    public void pinBoard(Long boardId, boolean isPinned) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));


        if (!board.getCategory().equals(Category.NOTICE)) {
            throw new IllegalArgumentException("공지사항 게시물만 상단 고정이 가능합니다.");
        }

        board.setIsPinned(isPinned);
        boardRepository.save(board);
    }
}


