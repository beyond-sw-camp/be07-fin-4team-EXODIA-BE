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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
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


    @Autowired
    public BoardService(BoardRepository boardRepository, UploadAwsFileService uploadAwsFileService, BoardFileRepository boardFileRepository, UserRepository userRepository, CommentRepository commentRepository, BoardHitsService boardHitsService) {
        this.boardRepository = boardRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.boardFileRepository = boardFileRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.boardHitsService = boardHitsService;
    }



    // 게시물 생성
    @Transactional
    public Board createBoard(BoardSaveReqDto dto, List<MultipartFile> files) {
        Category category = dto.getCategory();

        User user = userRepository.findByUserNum(dto.getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        if ((category == Category.NOTICE || category == Category.FAMILY_EVENT) &&
                !user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 경조사 게시물은 인사팀만 작성할 수 있습니다.");
        }

        // BoardSaveReqDto에서 엔티티 변환
        Board board = dto.toEntity(user, category);
        board = boardRepository.save(board);

        // Redis 조회수 및 사용자 조회 기록 초기화
        boardHitsService.resetBoardHits(board.getId());

        // 파일이 있는 경우 파일 처리
        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files);

            // BoardFile 엔티티를 생성하여 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                // BoardFile 엔티티 생성 및 저장
                BoardFile boardFile = BoardFile.createBoardFile(
                        board,
                        s3FilePath,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        file.getSize()
                );

                // BoardFile 저장
                boardFileRepository.save(boardFile);
            }
        }

        // 최종적으로 저장된 Board 엔티티 반환
        return board;
    }





    public Page<BoardListResDto> BoardListWithSearch(Pageable pageable, String searchType, String searchQuery) {
        Page<Board> boards;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    boards = boardRepository.findByTitleContainingIgnoreCase(searchQuery, DelYN.N, pageable);
                    break;
                case "content":
                    boards = boardRepository.findByContentContainingIgnoreCase(searchQuery, DelYN.N, pageable);
                    break;
                case "title+content":
                    boards = boardRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                            searchQuery, searchQuery, DelYN.N, pageable);
                    break;
                case "user_num":
                    if (searchQuery.length() != 12) {
                        throw new IllegalArgumentException("사번은 12자리 문자열이어야 합니다.");
                    }
                    boards = boardRepository.findByUser_UserNumAndDelYn(searchQuery, DelYN.N, pageable);
                    break;
                case "name":
                    boards = boardRepository.findByUser_NameContainingIgnoreCase(searchQuery, DelYN.N, pageable);
                    break;
                default:
                    boards = boardRepository.findAllWithPinned(pageable);  // 상단 고정 적용된 쿼리
                    break;
            }
        } else {
            boards = boardRepository.findAllWithPinned(pageable);  // 상단 고정 적용된 쿼리
        }

        // 조회수 최신화 작업
        return boards.map(board -> {
            Long currentHits = boardHitsService.getBoardHits(board.getId());
            board.updateBoardHitsFromRedis(currentHits); // 조회수 업데이트
            return board.listFromEntity(); // DTO 변환
        });
    }


    public BoardDetailDto BoardDetail(Long id) {
        // 게시물 조회
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        String user_num = SecurityContextHolder.getContext().getAuthentication().getName();

        // 조회수 증가 후 증가된 조회수 값 반환
        Long updatedHits = boardHitsService.incrementBoardHits(id, user_num);

        // 게시물의 조회수를 증가된 값으로 업데이트
        board.updateBoardHitsFromRedis(updatedHits);

        // 업데이트된 조회수를 DB에 반영
        boardRepository.save(board);

        // 관련 파일 목록 조회
        List<BoardFile> boardFiles = boardFileRepository.findByBoardId(id);

        // 댓글 리스트 조회
        List<Comment> comments = commentRepository.findByBoardId(id);
        List<CommentResDto> commentResDto = comments.stream()
                .map(CommentResDto::fromEntity)
                .collect(Collectors.toList());

        // 게시물 상세 정보 생성
        BoardDetailDto boardDetailDto = board.detailFromEntity(boardFiles);
        boardDetailDto.setComments(commentResDto);  // 댓글 리스트 추가
        boardDetailDto.setHits(updatedHits);  // 조회수 반영
        boardDetailDto.setUser_num(user_num);

        return boardDetailDto;
    }





    @Transactional
    public void updateBoard(Long id, BoardUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // 게시물 조회
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 사용자 권한 확인
        if (!board.getUser().getUserNum().equals(userNum)) {
            System.out.println("사용자 번호 불일치: " + userNum + " vs " + board.getUser().getUserNum());
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        User user = userRepository.findByUserNum(board.getUser().getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        Category category = dto.getCategory();

        // 카테고리가 NOTICE 또는 FAMILY_EVENT일 때, 부서가 인사팀인지 확인
        if ((category == Category.NOTICE || category == Category.FAMILY_EVENT) &&
                !user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 가족 행사 게시물은 인사팀만 작성할 수 있습니다.");
        }

        board = dto.updateFromEntity(category,user);
        System.out.println(board.getCategory());
        board = boardRepository.save(board);


        boardFileRepository.deleteByBoardId(board.getId());

        List<String> s3FilePaths = null;
        // Step 3: 새로운 파일이 있는 경우 처리
        if (files != null && !files.isEmpty()) {
            s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files);

            // 새로운 파일 저장
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
            boardRepository.save(board); // 게시물 저장
        }
    }




    @Transactional
    public void pinBoard(Long boardId, Long userId, boolean isPinned) {
        // 게시물 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!user.getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("상단 고정은 인사팀만 가능합니다.");
        }

        if (!board.getCategory().equals(Category.NOTICE)) {
            throw new IllegalArgumentException("공지사항 게시물만 상단 고정이 가능합니다.");
        }

        board.setIsPinned(isPinned);
        boardRepository.save(board);
    }



    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        // 게시물의 카테고리가 NOTICE 또는 FAMILY_EVENT일 때, 부서가 인사팀인지 확인
        if ((board.getCategory() == Category.NOTICE || board.getCategory() == Category.FAMILY_EVENT) &&
                !board.getUser().getDepartment().getName().equals("인사팀")) {
            throw new SecurityException("공지사항 또는 가족 행사 게시물은 인사팀만 삭제할 수 있습니다.");
        }

        // 게시물 삭제
        boardRepository.delete(board);
    }



}