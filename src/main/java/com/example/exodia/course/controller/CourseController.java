package com.example.exodia.course.controller;

import com.example.exodia.course.domain.Course;
import com.example.exodia.course.dto.CourseCreateDto;
import com.example.exodia.course.dto.CourseListDto;
import com.example.exodia.course.dto.CourseUpdateDto;
import com.example.exodia.course.service.CourseService;
import com.example.exodia.registration.dto.RegistrationDto;
import com.example.exodia.registration.repository.RegistrationRepository;
import com.example.exodia.registration.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;

    public CourseController(CourseService courseService, RegistrationService registrationService, RegistrationRepository registrationRepository) {
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.registrationRepository = registrationRepository;
    }

    /* 강좌 생성 */
    @PostMapping("/create")
    public ResponseEntity<CourseListDto> createCourse(@RequestBody CourseCreateDto courseCreateDto) {
        Course createdCourse = courseService.createCourse(courseCreateDto);
        int currentParticipants = 0;
        CourseListDto createdCourseDto = CourseListDto.fromEntity(createdCourse, currentParticipants);
        return ResponseEntity.ok(createdCourseDto);
    }

    /* 강좌 업데이트  */
    @PutMapping("/update/{courseId}")
    public ResponseEntity<CourseListDto> updateCourse(@RequestBody CourseUpdateDto courseUpdateDto, @PathVariable Long courseId) {
        CourseListDto updatedCourse = courseService.updateCourse(courseUpdateDto, courseId);
        return ResponseEntity.ok(updatedCourse);
    }

    /* 강좌 리스트 */
    @GetMapping("/list")
    public ResponseEntity<List<CourseListDto>> listCourses() {
        List<CourseListDto> courses = courseService.listCourses();
        return ResponseEntity.ok(courses);
    }

    /* 강좌 삭제 */
    @PutMapping("/delete/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.softDeleteCourse(courseId);
        return ResponseEntity.ok().build();
    }

    /* 강좌 신청 */
    @PostMapping("/register/{courseId}")
    public ResponseEntity<String> registerParticipant(@PathVariable Long courseId) {
        String result = registrationService.registerParticipant(courseId);
        return ResponseEntity.ok(result);
    }

    /* 강좌 확정 인원 조회*/
    @GetMapping("/{courseId}/confirmed")
    public ResponseEntity<List<RegistrationDto>> getConfirmedParticipants(@PathVariable Long courseId) {
        List<RegistrationDto> confirmedParticipants = registrationService.getConfirmedParticipants(courseId);
        return ResponseEntity.ok(confirmedParticipants);
    }

}