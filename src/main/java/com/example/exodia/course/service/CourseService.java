package com.example.exodia.course.service;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.course.domain.Course;
import com.example.exodia.course.dto.CourseCreateDto;
import com.example.exodia.course.dto.CourseListDto;
import com.example.exodia.course.dto.CourseUpdateDto;
import com.example.exodia.course.repository.CourseRepository;
import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository, UserService userService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /* 강좌 생성 */
    @Transactional
    public Course createCourse(CourseCreateDto dto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        userService.checkHrAuthority(user.getDepartment().getId().toString());

//        if (courseRepository.findByName(createDto.getName()).isPresent()) {
//            throw new IllegalArgumentException("이미 존재하는 강의명입니다.");
//        }
        Course course = dto.toEntity(user);
        return courseRepository.save(course);
    }

    /* 강좌 업데이트 */
    @Transactional
    public CourseListDto updateCourse(CourseUpdateDto dto, Long courseId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강좌입니다."));

        userService.checkHrAuthority(user.getDepartment().getId().toString());

        Course updatedCourse = dto.toEntity(existingCourse, user);
        return CourseListDto.fromEntity(courseRepository.save(updatedCourse));
    }

    /* 강좌 리스트*/
    public List<CourseListDto> listCourses() {
        // 강좌 리스트는 모든 유저가 다 볼 수 있어야함
        return courseRepository.findByDelYn(DelYN.N).stream()
                .map(CourseListDto::fromEntity)
                .collect(Collectors.toList());
    }

    /* 강좌 삭제 */
    @Transactional
    public void softDeleteCourse(Long courseId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        userService.checkHrAuthority(user.getDepartment().getId().toString());

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강좌입니다."));

        // 강좌 생성자와 요청한 사용자가 동일한지 체크하거나 관리자 권한 체크
        if (!course.getUser().getUserNum().equals(userNum)) {
            throw new SecurityException("해당 강좌를 삭제할 권한이 없습니다.");
        }
        course.softDelete();
        courseRepository.save(course);
    }

}
