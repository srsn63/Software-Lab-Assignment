package com.academic.academic_management.controller;

import com.academic.academic_management.dto.CourseDto;
import com.academic.academic_management.dto.StudentDto;
import com.academic.academic_management.entity.*;
import com.academic.academic_management.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;

    public StudentController(StudentRepository studentRepo, UserRepository userRepo, CourseRepository courseRepo) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }

    @GetMapping("/me")
    public ResponseEntity<StudentDto> getProfile() {
        User u = getCurrentUser();
        Student s = studentRepo.findByUserId(u.getId());

        Set<CourseDto> courseDtos = s.getCourses().stream()
                .map(c -> CourseDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .teacherName(
                                c.getTeacher() != null ?
                                        c.getTeacher().getFullName() : null)
                        .build())
                .collect(java.util.stream.Collectors.toSet());

        StudentDto dto = StudentDto.builder()
                .id(s.getId())
                .fullName(s.getFullName())
                .username(u.getUsername())
                .courses(courseDtos)
                .build();

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<StudentDto> updateProfile(@RequestBody StudentDto update) {
        User u = getCurrentUser();
        Student s = studentRepo.findByUserId(u.getId());

        s.setFullName(update.getFullName());
        studentRepo.save(s);

        return getProfile();
    }


    @PostMapping("/me/enroll/{courseId}")
    public ResponseEntity<?> enroll(@PathVariable Long courseId) {
        User u = getCurrentUser();
        Student s = studentRepo.findByUserId(u.getId());
        Course c = courseRepo.findById(courseId).orElseThrow();
        s.getCourses().add(c);
        studentRepo.save(s);
        return ResponseEntity.ok("enrolled");
    }

    @PostMapping("/me/drop/{courseId}")
    public ResponseEntity<?> drop(@PathVariable Long courseId) {
        User u = getCurrentUser();
        Student s = studentRepo.findByUserId(u.getId());
        Course c = courseRepo.findById(courseId).orElseThrow();
        s.getCourses().remove(c);
        studentRepo.save(s);
        return ResponseEntity.ok("dropped");
    }
}

