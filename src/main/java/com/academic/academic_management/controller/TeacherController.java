package com.academic.academic_management.controller;


import com.academic.academic_management.dto.CourseDto;
import com.academic.academic_management.dto.TeacherDto;
import com.academic.academic_management.entity.*;
import com.academic.academic_management.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherRepository teacherRepo;
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;

    public TeacherController(TeacherRepository teacherRepo, UserRepository userRepo, CourseRepository courseRepo) {
        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }

    @GetMapping("/me")
    public ResponseEntity<TeacherDto> getMe() {
        User u = getCurrentUser();
        Teacher t = teacherRepo.findByUserId(u.getId());

        Set<CourseDto> courseDtos = t.getCourses().stream()
                .map(c -> CourseDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .teacherName(t.getFullName())
                        .build())
                .collect(java.util.stream.Collectors.toSet());

        TeacherDto dto = TeacherDto.builder()
                .id(t.getId())
                .fullName(t.getFullName())
                .username(u.getUsername())
                .courses(courseDtos)
                .build();

        return ResponseEntity.ok(dto);
    }


    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody Teacher update) {
        User u = getCurrentUser();
        Teacher t = teacherRepo.findByUserId(u.getId());
        t.setFullName(update.getFullName());
        teacherRepo.save(t);
        return ResponseEntity.ok(t);
    }

    @PostMapping("/me/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        User u = getCurrentUser();
        Teacher t = teacherRepo.findByUserId(u.getId());
        course.setTeacher(t);
        courseRepo.save(course);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/me/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @RequestBody Course update) {
        User u = getCurrentUser();
        Teacher t = teacherRepo.findByUserId(u.getId());
        Course c = courseRepo.findById(courseId).orElseThrow();
        if (c.getTeacher() == null || !c.getTeacher().getId().equals(t.getId())) {
            return ResponseEntity.status(403).body("not allowed to modify this course");
        }
        c.setTitle(update.getTitle());
        c.setDescription(update.getDescription());
        courseRepo.save(c);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<CourseDto>> myCourses() {
        User u = getCurrentUser();
        Teacher t = teacherRepo.findByUserId(u.getId());

        List<CourseDto> list = courseRepo.findByTeacherId(t.getId())
                .stream()
                .map(c -> CourseDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .teacherName(t.getFullName())
                        .build())
                .toList();

        return ResponseEntity.ok(list);
    }

}
