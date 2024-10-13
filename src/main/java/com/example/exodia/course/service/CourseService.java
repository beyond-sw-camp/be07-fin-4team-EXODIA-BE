package com.example.exodia.course.service;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.course.domain.Course;
import com.example.exodia.course.dto.CourseCreateDto;
import com.example.exodia.course.dto.CourseListDto;
import com.example.exodia.course.dto.CourseUpdateDto;
import com.example.exodia.course.repository.CourseRepository;
import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.registration.dto.RegistrationDto;
import com.example.exodia.registration.repository.RegistrationRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository, UserService userService, RegistrationRepository registrationRepository, @Qualifier("12") RedisTemplate<String, Object> redisTemplate) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.registrationRepository = registrationRepository;
        this.redisTemplate = redisTemplate;
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
        Course savedCourse = courseRepository.save(course);

        String redisKey = "course:" + savedCourse.getId() + ":participants";
        // TTL 설정, 강좌는 14일 정도 후 자동 삭제
        redisTemplate.opsForValue().set(redisKey, 0, 14, TimeUnit.DAYS);

        return savedCourse;
    }

    /* 강좌 업데이트 */
    public CourseListDto updateCourse(CourseUpdateDto dto, Long courseId) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강좌입니다."));

        userService.checkHrAuthority(user.getDepartment().getId().toString());


        Course updatedCourse = dto.toEntity(existingCourse, user);
        // 현재 참가자 수 계산
        int currentParticipants = registrationRepository.countByCourse(updatedCourse);

        return CourseListDto.fromEntity(courseRepository.save(updatedCourse), currentParticipants);
    }

    /* 강좌 리스트*/
    public List<CourseListDto> listCourses() {
        return courseRepository.findByDelYn(DelYN.N).stream().map(course -> {
                    int currentParticipants = registrationRepository.countByCourse(course);
                    return CourseListDto.fromEntity(course, currentParticipants);
                })
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
