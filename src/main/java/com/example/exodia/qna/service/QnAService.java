package com.example.exodia.qna.service;

import com.example.exodia.board.domain.BoardFile;
import com.example.exodia.board.repository.BoardFileRepository;
import com.example.exodia.comment.domain.Comment;
import com.example.exodia.comment.dto.CommentResDto;
import com.example.exodia.comment.repository.CommentRepository;
import com.example.exodia.common.domain.DelYN;
import com.example.exodia.common.service.KafkaProducer;
import com.example.exodia.common.service.UploadAwsFileService;
import com.example.exodia.department.domain.Department;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.qna.domain.Manager;
import com.example.exodia.qna.domain.QnA;
import com.example.exodia.qna.dto.*;
import com.example.exodia.qna.repository.ManagerRepository;
import com.example.exodia.qna.repository.QnARepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class QnAService {

    private final QnARepository qnARepository;
    private final CommentRepository commentRepository;
    private final UploadAwsFileService uploadAwsFileService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BoardFileRepository boardFileRepository;
    private final ManagerRepository managerRepository;
    private final KafkaProducer kafkaProducer;

    @Autowired
    public QnAService(QnARepository qnARepository, CommentRepository commentRepository,
                      UploadAwsFileService uploadAwsFileService, UserRepository userRepository,
                      DepartmentRepository departmentRepository, BoardFileRepository boardFileRepository, ManagerRepository managerRepository, KafkaProducer kafkaProducer) {
        this.qnARepository = qnARepository;
        this.commentRepository = commentRepository;
        this.uploadAwsFileService = uploadAwsFileService;
        this.userRepository = userRepository;
        this.boardFileRepository = boardFileRepository;
        this.departmentRepository = departmentRepository;
        this.managerRepository = managerRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public QnA createQuestion(QnASaveReqDto dto, List<MultipartFile> files, String userNum) {
        User questioner = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        if (dto.getDepartmentId() == null || dto.getDepartmentId() == 0) {
            throw new IllegalArgumentException("유효하지 않은 부서 ID 입니다.");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 부서를 찾을 수 없습니다."));

        QnA qna = dto.toEntity(questioner, department);

        // 파일 처리 로직 (필요 시 추가)
        files = files == null ? Collections.emptyList() : files;
        if (!files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty()) {
                    continue;
                }

                String s3FilePath = s3FilePaths.get(i);
                BoardFile boardFile = BoardFile.createQuestionFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                qna.getQuestionerFiles().add(boardFile);
                boardFileRepository.save(boardFile);
            }
        }
        qnARepository.save(qna);

        // 알림 전송: 해당 부서의 모든 매니저에게 알림
        List<User> managers = userRepository.findManagersByDepartmentId(department.getId());
        String message = "Q&A 질문이 도착했습니다: ";
        kafkaProducer.sendQnaEvent("QUESTION_REGISTERED", department.getId().toString(), userNum, message);

        return qna;
    }

    public Page<QnAListResDto> qnaListByGroup(Long departmentId, Pageable pageable) {
        Department department = departmentRepository.findDepartmentById(departmentId);
        Page<QnA> qnAS = qnARepository.findAllByDepartmentIdAndDelYN(department.getId(), DelYN.N, pageable);
        return qnAS.map(QnA::listFromEntity);
    }

    public Page<QnAListResDto> qnaListWithSearch(Pageable pageable, String searchType, String searchQuery) {
        Page<QnA> qnAS;

        if (searchQuery == null || searchQuery.isEmpty()) {
            Pageable sortedByDate = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            qnAS = qnARepository.findByDelYN(DelYN.N, sortedByDate);
        } else {
            qnAS = switch (searchType) {
                case "title" -> qnARepository.findByTitleContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable);
                case "content" -> qnARepository.findByQuestionTextContainingIgnoreCaseAndDelYN(searchQuery, DelYN.N, pageable);
                case "title + content" -> qnARepository.findByTitleContainingIgnoreCaseOrQuestionTextContainingIgnoreCaseAndDelYN(searchQuery, searchQuery, DelYN.N, pageable);
                default -> qnARepository.findByDelYN(DelYN.N, pageable);
            };
        }

        return qnAS.map(QnA::listFromEntity);
    }

    public List<QnAListResDto> getUserQnAs(String userNum) {
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));
        List<QnA> qnAs = qnARepository.findByQuestioner(user);
        return qnAs.stream().map(QnA::listFromEntity).collect(Collectors.toList());
    }

    public QnADetailDto getQuestionDetail(Long id) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        List<Comment> comments = commentRepository.findByQnaId(id);
        List<CommentResDto> commentResDto = comments.stream().map(CommentResDto::fromEntity).collect(Collectors.toList());
        return QnADetailDto.fromEntity(qna, commentResDto);
    }

    @Transactional
    public QnA answerQuestion(Long questionId, QnAAnswerReqDto dto, List<MultipartFile> files, String userNum) {
        QnA qna = qnARepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 답변자를 사번으로 찾음
        User answerer = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번을 가진 유저가 없습니다."));

        // 사용자가 매니저 테이블에 있는지 확인
        boolean isManager = managerRepository.existsByUser(answerer);
        if (!isManager) {
            throw new SecurityException("매니저만 질문에 답변할 권한이 있습니다.");
        }

        // 부서 확인 없이 답변 설정
        qna.setAnswerText(dto.getAnswerText());
        qna.setAnsweredAt(LocalDateTime.now());
        qna.setAnswerer(answerer);

        // 파일 처리 로직
        files = files == null ? Collections.emptyList() : files;
        if (!files.isEmpty()) {
            List<String> s3FilePaths = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(files, "qna");
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty()) {
                    continue;
                }

                String s3FilePath = s3FilePaths.get(i);
                BoardFile boardFile = BoardFile.createAnswerFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                qna.getAnswererFiles().add(boardFile);
                boardFileRepository.save(boardFile);
            }
        }

        // 질문자에게 답변 알림 전송
        String message = "질문에 대한 답변이 등록되었습니다.";
        kafkaProducer.sendQnaEvent("ANSWER_REGISTERED", qna.getDepartment().getId().toString(), qna.getQuestioner().getUserNum(), message);

        return qnARepository.save(qna);
    }

    @Transactional
    public void QnAQUpdate(Long id, QnAQtoUpdateDto dto, List<MultipartFile> files, String userNum) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        if (!qna.getQuestioner().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        qna.QnAQUpdate(dto);

        files = files == null ? Collections.emptyList() : files;
        if (!files.isEmpty()) {
            boardFileRepository.deleteAll(qna.getQuestionerFiles());
            qna.getQuestionerFiles().clear();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    System.out.println("빈 파일이므로 업로드를 건너뜁니다. 파일 이름: " + file.getOriginalFilename());
                    continue;
                }

                String s3FilePath = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(List.of(file), "qna").get(0);
                BoardFile boardFile = BoardFile.createQuestionFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                qna.getQuestionerFiles().add(boardFile);
                boardFileRepository.save(boardFile);
            }
        }

        qnARepository.save(qna);
    }

    @Transactional
    public void QnAAUpdate(Long id, QnAAtoUpdateDto dto, List<MultipartFile> files, String userNum) {
        QnA qna = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        if (!qna.getAnswerer().getUserNum().equals(userNum)) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        qna.QnAAUpdate(dto);  // updatedAt을 수동 갱신 (BaseTimeEntity의 메서드 사용)

        files = files == null ? Collections.emptyList() : files;
        if (!files.isEmpty()) {
            boardFileRepository.deleteAll(qna.getAnswererFiles());
            qna.getAnswererFiles().clear();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    System.out.println("빈 파일이므로 업로드를 건너뜁니다. 파일 이름: " + file.getOriginalFilename());
                    continue;
                }

                String s3FilePath = uploadAwsFileService.uploadMultipleFilesAndReturnPaths(List.of(file), "qna").get(0);
                BoardFile boardFile = BoardFile.createAnswerFile(qna, s3FilePath, file.getContentType(),
                        file.getOriginalFilename(), file.getSize());
                qna.getAnswererFiles().add(boardFile);
                boardFileRepository.save(boardFile);
            }
        }

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
