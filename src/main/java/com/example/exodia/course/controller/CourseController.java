    package com.example.exodia.course.controller;

    import com.example.exodia.course.dto.CourseCreateDto;
    import com.example.exodia.course.dto.CourseListDto;
    import com.example.exodia.course.dto.CourseUpdateDto;
    import com.example.exodia.course.service.CourseService;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /* 강좌 생성 */
    @PostMapping("/create")
    public ResponseEntity<CourseListDto> createCourse(@RequestBody CourseCreateDto courseCreateDto) {
        CourseListDto createdCourse = CourseListDto.fromEntity(courseService.createCourse(courseCreateDto));
        return ResponseEntity.ok(createdCourse);
    }

    /* 강좌 업데이트  */
    @PutMapping("/update/{courseId}")
    public ResponseEntity<CourseListDto> updateCourse(@RequestBody CourseUpdateDto courseUpdateDto,
                                                      @PathVariable Long courseId) {
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
}