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
import com.example.exodia.notification.domain.NotificationType;
import com.example.exodia.notification.dto.NotificationDTO;
import com.example.exodia.notification.service.NotificationService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final NotificationService notificationService;
    @Autowired
    public BoardService(BoardRepository boardRepository, UploadAwsFileService uploadAwsFileService,
                        BoardFileRepository boardFileRepository, UserRepository userRepository,
                        CommentRepository commentRepository, BoardHitsService boardHitsService,
                        BoardTagRepository boardTagRepository, BoardTagsRepository boardTagsRepository, KafkaProducer kafkaProducer, NotificationService notificationService) {
        this.boardRepository = boardRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.boardFileRepository = boardFileRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.boardHitsService = boardHitsService;
        this.boardTagRepository = boardTagRepository;
        this.boardTagsRepository = boardTagsRepository;
        this.kafkaProducer = kafkaProducer;
        this.notificationService = notificationService;
    }

    @Transactional
    public Board createBoard(BoardSaveReqDto dto, List<MultipartFile> files, List<Long> tagIds) {
        User user = validateUserAndCategory(dto.getUserNum(), dto.getCategory());
        Board board = dto.toEntity(user);
        board = boardRepository.save(board);

        if (tagIds != null && !tagIds.isEmpty()) {
            addTagsToBoard(board, tagIds);
        }

        processFiles(files, board);
        boardHitsService.resetBoardHits(board.getId());

        String departmentName = (user.getDepartment() != null) ? user.getDepartment().getName() : "부서 없음";
        String title = (dto.getTitle() != null) ? dto.getTitle() : "제목 없음";


        String message = departmentName + " 에서 " + title + "를 작성했습니다";

        /**/
        Long targetPath = board.getId();
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .message(message)
                .type(NotificationType.공지사항)
                .isRead(false)
                .userName(user.getName())
                .userNum(user.getUserNum())
                .notificationTime(LocalDateTime.now())
                .targetId(board.getId())
                .build();

        notificationService.saveNotification(user.getUserNum(), notificationDTO);
        /**/

        kafkaProducer.sendBoardEvent("notice-events", message);
        return board;
    }

    @Transactional
    private void addTagsToBoard(Board board, List<Long> tagIds) {
        List<BoardTags> tags = boardTagsRepository.findAllById(tagIds);

        for (BoardTags tag : tags) {
            BoardTag boardTag = BoardTag.builder()
                    .board(board)
                    .boardTags(tag)
                    .build();
            boardTagRepository.save(boardTag);
        }
    }


    @Transactional
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


    private User validateUserAndCategory(String userNum, Category category) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        if ((category == Category.NOTICE || category == Category.FAMILY_EVENT) &&
                !user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 경조사 게시물은 인사팀만 작성할 수 있습니다.");
        }

        return user;
    }

    @Transactional
    public Page<BoardListResDto> BoardListWithSearch(Pageable pageable, String searchType, String searchQuery, Category category, List<Long> tagIds) {
        Page<Board> boards;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalseAndTitleContainingIgnoreCase(
                            category, DelYN.N, searchQuery, pageable);
                    break;
                case "content":
                    boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalseAndContentContainingIgnoreCase(
                            category, DelYN.N, searchQuery, pageable);
                    break;
                case "tags":
                    boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalseAndTagsContainingIgnoreCase(
                            category, DelYN.N, searchQuery, pageable);
                    break;
                case "title + content":
                    boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalseAndTitleOrContentContainingIgnoreCase(
                            category, DelYN.N, searchQuery, pageable);
                    break;
                default:
                    boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalse(category, DelYN.N, pageable);
                    break;
            }
        } else {
            boards = boardRepository.findByCategoryAndDelYnAndIsPinnedFalse(category, DelYN.N, pageable);
        }

        return boards.map(Board::listFromEntity);
    }


    @Transactional
    public BoardDetailDto BoardDetail(Long id, String userNum) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));


        Long updatedHits = boardHitsService.incrementBoardHits(id, userNum);
        board.updateBoardHitsFromRedis(updatedHits);
        boardRepository.save(board);


        List<BoardFile> boardFiles = boardFileRepository.findByBoardId(id);
        List<Comment> comments = commentRepository.findByBoardId(id);
        List<CommentResDto> commentResDto = comments.stream()
                .map(CommentResDto::fromEntity)
                .collect(Collectors.toList());


        List<BoardTags> tags = boardTagRepository.findByBoardId(id)
                .stream()
                .map(boardTag -> boardTag.getBoardTags())
                .collect(Collectors.toList());

        List<String> tagNames = tags.stream()
                .map(BoardTags::getTag)
                .collect(Collectors.toList());


        BoardDetailDto boardDetailDto = board.detailFromEntity(boardFiles);
        boardDetailDto.setComments(commentResDto);
        boardDetailDto.setHits(updatedHits);
        boardDetailDto.setUser_num(board.getUser().getUserNum());
        boardDetailDto.setTags(tagNames);

        return boardDetailDto;
    }


    @Transactional
    public void updateBoard(Long id, BoardUpdateDto dto, List<MultipartFile> files, List<Long> tagIds, String userNum) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));


        if (!board.getUser().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }


        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setCategory(dto.getCategory());


        board.updateTimestamp();


        boardTagRepository.deleteByBoardId(board.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            addTagsToBoard(board, tagIds);
        }


        processFiles(files, board);

        boardRepository.save(board);
    }


    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        boardTagRepository.deleteByBoardId(id);
        boardRepository.softDeleteById(id);
        boardRepository.save(board);
    }


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

    // BoardService.java
    @Transactional
    public List<BoardListResDto> getPinnedBoards() {
        // 고정된 게시물 필터링 및 변환
        List<Board> pinnedBoards = boardRepository.findByIsPinnedTrue(Sort.by(Sort.Direction.DESC, "createdAt"));
        return pinnedBoards.stream()
                .map(board -> BoardListResDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .category(board.getCategory())
                        .hits(board.getHits())
                        .user_num(board.getUser().getUserNum())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .isPinned(board.getIsPinned())
                        .build())

                .collect(Collectors.toList());
    }

    public long getTotalBoardCount(String category) {
        Category boardCategory;

        // 카테고리 변환
        if ("familyevent".equalsIgnoreCase(category)) {
            boardCategory = Category.FAMILY_EVENT;
        } else if ("notice".equalsIgnoreCase(category)) {
            boardCategory = Category.NOTICE;
        } else {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        // 해당 카테고리와 삭제 여부(N) 기준으로 전체 게시물 개수를 반환
        return boardRepository.countByCategoryAndDelYn(boardCategory, DelYN.N);
    }

}


