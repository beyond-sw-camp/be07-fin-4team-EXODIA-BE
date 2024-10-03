package com.example.exodia.board.service;

import com.example.exodia.board.domain.Board;
import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.board.domain.Category;
import com.example.exodia.board.dto.BoardDetailDto;
import com.example.exodia.board.dto.BoardListResDto;
import com.example.exodia.board.dto.BoardSaveReqDto;
import com.example.exodia.board.dto.BoardUpdateDto;
import com.example.exodia.board.repository.BoardFileRepository;
import com.example.exodia.board.repository.BoardRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentResDto;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import io.lettuce.core.ScriptOutputType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service // 해당 클래스가 서비스 레이어의 역할을 수행하며, 스프링 빈으로 등록됨을 나타냄
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final UploadAwsFileService uploadAwsFileService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    @Autowired
    public BoardService(BoardRepository boardRepository, UploadAwsFileService uploadAwsFileService,
                        BoardFileRepository boardFileRepository, UserRepository userRepository,
                        CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.boardFileRepository = boardFileRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;

    }

    /**
     * 새로운 게시물을 생성하는 메서드
     * @param dto - 사용자가 작성한 게시물 정보 객체 (제목, 내용, 카테고리 등)
     * @param files - 사용자가 업로드한 파일 리스트
     * @return 생성된 게시물 객체
     */
    @Transactional
    public Board createBoard(BoardSaveReqDto dto, List<MultipartFile> files) {
        Category category = dto.getCategory();

        // 사용자의 사번을 통해 사용자 정보 조회 (존재하지 않으면 예외 발생)
        User user = userRepository.findByUserNum(dto.getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 카테고리가 '공지사항' 또는 '경조사'일 경우, 작성자가 '인사팀' 소속인지 확인
        if ((category == Category.NOTICE || category == Category.FAMILY_EVENT) &&
                !user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 경조사 게시물은 인사팀만 작성할 수 있습니다.");
        }

        // 게시물 정보 저장
        Board board = dto.toEntity(user, category);
        board = boardRepository.save(board);

        // 업로드할 파일 리스트가 null이거나 비어 있는지 확인하고, 실제 업로드할 파일 리스트만 필터링
        List<MultipartFile> validFiles = files != null ?
                files.stream()
                        .filter(file -> !file.isEmpty())  // 빈 파일 제외
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        // 실제 업로드할 파일이 있는 경우에만 S3 업로드 수행
        if (!validFiles.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(validFiles, "board");

            // 파일 리스트 크기와 s3FilePaths 크기가 일치하는지 확인 (업로드된 경로 수가 파일 수와 같아야 함)
            if (validFiles.size() == s3FilePaths.size()) {
                for (int i = 0; i < validFiles.size(); i++) {
                    MultipartFile file = validFiles.get(i);
                    String s3FilePath = s3FilePaths.get(i);

                    // BoardFile 엔티티 생성 및 저장
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

        return board;
    }


    /**
     * 검색 조건에 따라 게시물 목록을 조회하는 메서드
     * @param pageable - 페이징 및 정렬 정보
     * @param searchType - 검색 유형 (예: 제목, 내용, 사용자 이름 등)
     * @param searchQuery - 검색어
     * @return 검색 조건에 따른 게시물 목록 (Page 형태로 반환)
     */
    public Page<BoardListResDto> BoardListWithSearch(Pageable pageable, String searchType, String searchQuery, Category category) {
        Page<Board> boards;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    boards = boardRepository.findByTitleContainingIgnoreCaseAndCategory(searchQuery, category, DelYN.N, pageable);
                    break;
                case "content":
                    boards = boardRepository.findByContentContainingIgnoreCaseAndCategory(searchQuery, category, DelYN.N, pageable);
                    break;
                case "title+content":
                    boards = boardRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndCategory(
                            searchQuery, searchQuery, category, DelYN.N, pageable);
                    break;
                case "user_num":
                    if (searchQuery.length() != 12) {
                        throw new IllegalArgumentException("사번은 12자리 문자열이어야 합니다.");
                    }
                    boards = boardRepository.findByUser_UserNumAndCategoryAndDelYn(searchQuery, category, DelYN.N, pageable);
                    break;
                case "name":
                    boards = boardRepository.findByUser_NameContainingIgnoreCaseAndCategory(searchQuery, category, DelYN.N, pageable);
                    break;
                default:
                    boards = boardRepository.findByCategoryAndDelYn(category, DelYN.N, pageable);
                    break;
            }
        } else if (category != null) {
            boards = boardRepository.findByCategoryAndDelYn(category, DelYN.N, pageable);
        } else {
            boards = boardRepository.findAllWithPinnedByCategory(category, pageable);
        }

        return boards.map(Board::listFromEntity);
    }








    /**
     * 특정 게시물의 상세 정보를 조회하는 메서드
     * @param id - 조회할 게시물의 고유 ID
     * @return 게시물의 상세 정보를 포함한 DTO
     */
    public BoardDetailDto BoardDetail(Long id) {
        // 게시물 조회 (존재하지 않을 경우 예외 발생)
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // 조회수 증가 후 업데이트된 조회수 반환
        Long updatedHits = 1L;

//        board.updateBoardHitsFromRedis(updatedHits);
        boardRepository.save(board);

        // 파일 및 댓글 정보 조회
        List<BoardFile> boardFiles = boardFileRepository.findByBoardId(id);
        List<Comment> comments = commentRepository.findByBoardId(id);
        List<CommentResDto> commentResDto = comments.stream()
                .map(CommentResDto::fromEntity)
                .collect(Collectors.toList());

        // 게시물 상세 정보 생성 후 반환
        BoardDetailDto boardDetailDto = board.detailFromEntity(boardFiles);
        boardDetailDto.setComments(commentResDto);
        boardDetailDto.setHits(updatedHits);
        boardDetailDto.setUser_num(userNum);

        return boardDetailDto;
    }

    /**
     * 기존 게시물 수정
     * @param id - 수정할 게시물의 고유 ID
     * @param dto - 수정할 게시물 정보 객체
     * @param files - 새롭게 추가할 파일 리스트
     */
    @Transactional
    public void updateBoard(Long id, BoardUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // 게시물 조회 (존재하지 않을 경우 예외 발생)
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 작성자와 현재 사용자가 동일한지 확인
        if (!board.getUser().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        User user = userRepository.findByUserNum(board.getUser().getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        Category category = dto.getCategory();

        // 게시물 정보 업데이트
        board = dto.updateFromEntity(category, user);
        board = boardRepository.save(board);

        // 기존 파일 삭제 및 새로운 파일 정보 저장
        boardFileRepository.deleteByBoardId(board.getId());

        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "board");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                BoardFile boardFile = BoardFile.builder()
                        .board(board)
                        .filePath(s3FilePath)
                        .fileType(file.getContentType())
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .build();

                boardFileRepository.save(boardFile);
            }
        }
    }

    /**
     * 게시물을 상단에 고정하거나 해제하는 메서드
     * @param boardId - 게시물의 고유 ID
     * @param userId - 상단 고정을 수행할 사용자의 ID
     * @param isPinned - 상단 고정 여부 (true: 고정, false: 해제)
     */
    @Transactional
    public void pinBoard(Long boardId, Long userId, boolean isPinned) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 인사팀 소속 사용자만 상단 고정 가능
        if (!user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("상단 고정은 인사팀만 가능합니다.");
        }

        // 공지사항 게시물만 상단 고정 가능
        if (!board.getCategory().equals(Category.NOTICE)) {
            throw new IllegalArgumentException("공지사항 게시물만 상단 고정이 가능합니다.");
        }

        board.setIsPinned(isPinned);
        boardRepository.save(board);
    }

    /**
     * 특정 게시물 삭제
     * @param id - 삭제할 게시물의 고유 ID
     */
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        // 공지사항 또는 가족 행사 게시물일 때, 작성자가 '인사팀' 소속인지 확인
        if ((board.getCategory() == Category.NOTICE || board.getCategory() == Category.FAMILY_EVENT) &&
                !board.getUser().getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 가족 행사 게시물은 인사팀만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
    }
}