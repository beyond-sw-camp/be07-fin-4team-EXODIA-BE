
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
    public QnAService(QnARepository qnARepository, UserService userService, CommentRepository commentRepository, UploadAwsFileService uploadAwsFileService, CommentService commentService, UserRepository userRepository, DepartmentRepository departmentRepository, BoardFileRepository boardFileRepository, PasswordEncoder passwordEncoder) {
        this.qnARepository = qnARepository;
        this.commentRepository = commentRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.userRepository = userRepository;
        this.boardFileRepository = boardFileRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public QnA createQuestion(QnASaveReqDto dto, List<MultipartFile> files,Department department) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // Department가 transient 상태인지 확인하고 영속화
        if (department.getId() == null) {
            departmentRepository.save(department);
        }

        QnA qna = dto.toEntity(user,department);

        // 파일이 있는 경우 파일 처리
        if (files != null && !files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files,"qna");

            // BoardFile 엔티티를 생성하여 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                // BoardFile 엔티티 생성 및 저장
                BoardFile boardFile = BoardFile.createQnAFile(
                        qna,
                        s3FilePath,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        file.getSize()
                );

                // BoardFile 저장
                boardFileRepository.save(boardFile);
            }
        }


        return qnARepository.save(qna);
    }



    public Page<QnAListResDto> qnaListByGroup(Long departmentId, Pageable pageable){
        Department department = departmentRepository.findDepartmentById(departmentId);
        Page<QnA> qnAS = qnARepository.findAllByDepartmentIdAndDelYN(department.getId(), DelYN.N, pageable );
        Page<QnAListResDto> qnAListResDtos = qnAS.map(a->a.listFromEntity());
        return qnAListResDtos;
    }

    public Page<QnAListResDto> qnaListWithSearch(Pageable pageable, String searchType, String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            switch (searchType) {
                case "title":
                    return qnARepository.findByTitleContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable)
                            .map(QnA::listFromEntity);
                case "content":
                    return qnARepository.findByQuestionTextContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable)
                            .map(QnA::listFromEntity);
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




    //사용자가 작성한 qna 목록 보기
    public List<QnAListResDto> getUserQnAs() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("UserNum: " + userNum); // userNum 값을 확인하기 위해 로그 추가
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        List<QnA> qnAs = qnARepository.findByQuestioner(user);
        return qnAs.stream().map(QnA::listFromEntity).collect(Collectors.toList()); // DTO로 변환
    }

    public QnADetailDto getQuestionDetail(Long id) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        List<Comment> comments = commentRepository.findByQnaId(id);
        List<CommentResDto> commentResDto = comments.stream()
                .map(CommentResDto::fromEntity)
                .collect(Collectors.toList());

        // QnA와 댓글을 사용해 QnADetailDto 생성
        QnADetailDto qnADetailDto = QnADetailDto.fromEntity(qna, commentResDto);
        return qnADetailDto;
    }


    // 답변 작성
    @Transactional
    public QnA answerQuestion(Long id, QnAAnswerReqDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // QnA 게시글 조회
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 질문자의 부서 정보
        Department questionerDepartment = qna.getQuestioner().getDepartment();

        // 답변자 조회
        User answerer = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 답변자와 질문자가 다른 부서일 경우 권한 없음
        if (!answerer.getDepartment().getId().equals(questionerDepartment.getId())) {
            throw new SecurityException("다른 부서의 질문에 답변할 권한이 없습니다.");
        }

        // 답변 텍스트 업데이트
        qna.setAnswerText(dto.getAnswerText()); // 답변 텍스트 업데이트
        qna.setAnsweredAt(LocalDateTime.now()); // 답변 시간을 현재 시간으로 설정
        qna.setAnswerer(answerer); // 답변자 설정

        // 파일이 있는 경우 파일 처리
        if (files != null && !files.isEmpty()) {

            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            // BoardFile 엔티티를 생성하여 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                // BoardFile 엔티티 생성
                BoardFile boardFile = BoardFile.createQnAFile(
                        qna,
                        s3FilePath,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        file.getSize()
                );

                // BoardFile 저장
                boardFileRepository.save(boardFile);
            }
        }

        // 변경된 QnA 저장
        return qnARepository.save(qna);
    }



    @Transactional
    public void QnAQUpdate(Long id, QnAQtoUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // QnA 게시글 조회
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 질문자 정보 가져오기
        User questioner = qna.getQuestioner();

        // 작성자가 맞는지 확인
        if (!questioner.getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        // 질문 내용 업데이트
        qna.QnAQUpdate(dto);

        // 파일이 있는 경우만 처리
        if (files != null && !files.isEmpty()) {

            // 새 파일 업로드
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            // BoardFile 엔티티 생성 및 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                // BoardFile 엔티티 생성
                BoardFile boardFile = BoardFile.createQnAFile(
                        qna,
                        s3FilePath,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        file.getSize()
                );

                // BoardFile 저장
                boardFileRepository.save(boardFile);
            }
        }

        // 변경된 QnA 저장
        qnARepository.save(qna);
    }






    @Transactional
    public void QnAAUpdate(Long id, QnAAtoUpdateDto dto, List<MultipartFile> files) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();

        // QnA 게시글 조회
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 질문자 정보 가져오기
        User answerer = qna.getQuestioner();

        // 작성자가 맞는지 확인
        if (!answerer.getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        // 질문 내용 업데이트
        qna.QnAAUpdate(dto);


        if (files != null && !files.isEmpty()) {

            // 새 파일 업로드
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");

            // BoardFile 엔티티 생성 및 저장
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3FilePath = s3FilePaths.get(i);

                // BoardFile 엔티티 생성
                BoardFile boardFile = BoardFile.createQnAFile(
                        qna,
                        s3FilePath,
                        file.getContentType(),
                        file.getOriginalFilename(),
                        file.getSize()
                );

                // BoardFile 저장
                boardFileRepository.save(boardFile);
            }
        }

        // 변경된 QnA 저장
        qnARepository.save(qna);
    }


    @Transactional
    public QnA qnaDelete(Long id) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        qna.updateDelYN(DelYN.Y);
        return qna;
    }
}
