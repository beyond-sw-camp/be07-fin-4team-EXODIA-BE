
package com.example.exodia.qna.service;


import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.board.repository.BoardFileRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentResDto;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.comment.service.CommentService;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.qna.domain.QnA;
import com.example.exodia.qna.dto.*;
import com.example.exodia.qna.repository.QnARepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;



import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QnAService {

    private final QnARepository qnARepository;
    private final CommentRepository commentRepository;
    private final UploadAwsFileService uploadAwsFileService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BoardFileRepository boardFileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public QnAService(QnARepository qnARepository, UserService userService, CommentRepository commentRepository,
                      UploadAwsFileService uploadAwsFileService, CommentService commentService,
                      UserRepository userRepository, DepartmentRepository departmentRepository,
                      BoardFileRepository boardFileRepository, PasswordEncoder passwordEncoder) {
        this.qnARepository = qnARepository;
        this.commentRepository = commentRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.userRepository = userRepository;
        this.boardFileRepository = boardFileRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 새로운 QnA 질문 생성
     * @param dto - 질문 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @param department - 질문을 등록할 부서 정보
     * @return 생성된 QnA 객체 반환
     */
    @Transactional
    public QnA createQuestion(QnASaveReqDto dto, List<MultipartFile> files, Department department) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName(); // 현재 로그인된 사용자의 사번 조회
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 부서 정보가 저장되지 않은 경우 영속화 처리
        if (department.getId() == null) {
            departmentRepository.save(department);
        }

        QnA qna = dto.toEntity(user, department); // DTO를 QnA 엔티티로 변환

        // 첨부 파일이 있는 경우 파일 저장
        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            // 각 파일에 대한 엔티티 생성 및 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                BoardFile boardFile = BoardFile.createQnAFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                boardFileRepository.save(boardFile);
            }
        }

        return qnARepository.save(qna); // QnA 저장 후 반환
    }

    /**
     * 특정 부서의 QnA 목록 조회
     * @param departmentId - 부서 ID
     * @param pageable - 페이징 정보
     * @return 조회된 QnA 목록 반환
     */
    public Page<QnAListResDto> qnaListByGroup(Long departmentId, Pageable pageable) {
        Department department = departmentRepository.findDepartmentById(departmentId);
        Page<QnA> qnAS = qnARepository.findAllByDepartmentIdAndDelYN(department.getId(), DelYN.N, pageable);
        return qnAS.map(QnA::listFromEntity); // DTO로 변환하여 반환
    }

    /**
     * 검색 조건에 따른 QnA 목록 조회
     * @param pageable - 페이징 정보
     * @param searchType - 검색 유형 (title, content 등)
     * @param searchQuery - 검색어
     * @return 조회된 QnA 목록 반환
     */
    public Page<QnAListResDto> qnaListWithSearch(Pageable pageable, String searchType, String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    return qnARepository.findByTitleContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable).map(QnA::listFromEntity);
                case "content":
                    return qnARepository.findByQuestionTextContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable).map(QnA::listFromEntity);
                case "title+content":
                    return qnARepository.findByTitleContainingIgnoreCaseOrQuestionTextContainingIgnoreCaseAndDelYN(
                            searchQuery, searchQuery, DelYN.N, pageable).map(QnA::listFromEntity);
                default:
                    return qnARepository.findByDelYN(DelYN.N, pageable).map(QnA::listFromEntity);
            }
        } else {
            return qnARepository.findByDelYN(DelYN.N, pageable).map(QnA::listFromEntity);
        }
    }

    /**
     * 사용자가 작성한 QnA 목록 조회
     * @return 사용자 작성 QnA 목록 반환
     */
    public List<QnAListResDto> getUserQnAs() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        List<QnA> qnAs = qnARepository.findByQuestioner(user);
        return qnAs.stream().map(QnA::listFromEntity).collect(Collectors.toList()); // DTO로 변환하여 반환
    }

    /**
     * 특정 QnA 질문의 상세 정보 조회
     * @param id - QnA ID
     * @return QnA의 상세 정보 및 관련 댓글 목록 반환
     */
    public QnADetailDto getQuestionDetail(Long id) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        List<Comment> comments = commentRepository.findByQnaId(id);
        List<CommentResDto> commentResDto = comments.stream().map(CommentResDto::fromEntity).collect(Collectors.toList());

        return QnADetailDto.fromEntity(qna, commentResDto); // QnA와 댓글 정보를 포함한 DTO 반환
    }

    /**
     * QnA에 답변 작성
     * @param id - QnA ID
     * @param dto - 답변 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     * @return 업데이트된 QnA 객체 반환
     */
    @Transactional
    public QnA answerQuestion(Long id, QnAAnswerReqDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        Department questionerDepartment = qna.getQuestioner().getDepartment();

        User answerer = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 답변자와 질문자가 다른 부서일 경우 권한 없음
        if (!answerer.getDepartment().getId().equals(questionerDepartment.getId())) {
            throw new SecurityException("다른 부서의 질문에 답변할 권한이 없습니다.");
        }

        qna.setAnswerText(dto.getAnswerText());
        qna.setAnsweredAt(LocalDateTime.now());
        qna.setAnswerer(answerer);

        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                BoardFile boardFile = BoardFile.createQnAFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                boardFileRepository.save(boardFile);
            }
        }

        return qnARepository.save(qna); // QnA 저장 후 반환
    }

    /**
     * QnA 질문 수정
     * @param id - QnA ID
     * @param dto - 수정할 질문 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     */
    @Transactional
    public void QnAQUpdate(Long id, QnAQtoUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 작성자 확인
        if (!qna.getQuestioner().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        qna.QnAQUpdate(dto);

        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                BoardFile boardFile = BoardFile.createQnAFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                boardFileRepository.save(boardFile);
            }
        }

        qnARepository.save(qna); // QnA 저장
    }

    /**
     * QnA 답변 수정
     * @param id - QnA ID
     * @param dto - 수정할 답변 정보가 담긴 객체
     * @param files - 첨부 파일 목록 (선택적)
     */
    @Transactional
    public void QnAAUpdate(Long id, QnAAtoUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 작성자 확인
        if (!qna.getAnswerer().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        qna.QnAAUpdate(dto);

        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                BoardFile boardFile = BoardFile.createQnAFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                boardFileRepository.save(boardFile);
            }
        }

        qnARepository.save(qna); // QnA 저장
    }

    /**
     * QnA 삭제
     * @param id - QnA ID
     * @return 삭제된 QnA 객체 반환
     */
    @Transactional
    public QnA qnaDelete(Long id) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        qna.updateDelYN(DelYN.Y); // QnA 삭제 처리
        return qna;
    }
}

