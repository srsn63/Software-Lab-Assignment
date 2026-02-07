package com.academic.academic_management.controller;

import com.academic.academic_management.dto.CourseDto;
import com.academic.academic_management.entity.Course;
import com.academic.academic_management.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository repo;

    public CourseController(CourseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> list() {

        List<CourseDto> courses = repo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> get(@PathVariable Long id) {

        Course course = repo.findById(id).orElseThrow();

        return ResponseEntity.ok(mapToDto(course));
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .teacherName(
                        course.getTeacher() != null
                                ? course.getTeacher().getFullName()
                                : null
                )
                .build();
    }
}
