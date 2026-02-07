package com.academic.academic_management.controller;


import com.academic.academic_management.dto.*;
import com.academic.academic_management.entity.*;
import com.academic.academic_management.repository.*;
import com.academic.academic_management.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager,
                          UserRepository userRepo,
                          StudentRepository studentRepo,
                          TeacherRepository teacherRepo,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register/students")
    public ResponseEntity<?> registerStudent(@RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("username already taken");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_STUDENT)
                .build();
        userRepo.save(u);
        Student s = Student.builder().fullName(req.getFullName()).user(u).build();
        studentRepo.save(s);
        return ResponseEntity.ok("student registered");
    }

    @PostMapping("/register/teachers")
    public ResponseEntity<?> registerTeacher(@RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("username already taken");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_TEACHER)
                .build();
        userRepo.save(u);
        Teacher t = Teacher.builder().fullName(req.getFullName()).user(u).build();
        teacherRepo.save(t);
        return ResponseEntity.ok("teacher registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            String token = jwtUtil.generateToken(req.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("invalid credentials");
        }
    }
}

