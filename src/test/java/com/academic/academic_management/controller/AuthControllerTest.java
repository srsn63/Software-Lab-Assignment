package com.academic.academic_management.controller;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academic.academic_management.dto.AuthRequest;
import com.academic.academic_management.dto.RegisterRequest;
import com.academic.academic_management.entity.Role;
import com.academic.academic_management.entity.User;
import com.academic.academic_management.repository.StudentRepository;
import com.academic.academic_management.repository.TeacherRepository;
import com.academic.academic_management.repository.UserRepository;
import com.academic.academic_management.security.CustomUserDetailsService;
import com.academic.academic_management.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authManager;

    @MockitoBean
    private UserRepository userRepo;

    @MockitoBean
    private StudentRepository studentRepo;

    @MockitoBean
    private TeacherRepository teacherRepo;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ─── Registration Tests ─────────────────────────────────────

    @Test
    void registerStudent_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("student1");
        req.setPassword("pass123");
        req.setFullName("Test Student");

        when(userRepo.findByUsername("student1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        mockMvc.perform(post("/api/auth/register/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("student registered"));
    }

    @Test
    void registerStudent_duplicateUsername_returnsBadRequest() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass123");
        req.setFullName("Duplicate User");

        User existingUser = User.builder()
                .id(1L).username("existing").password("enc").role(Role.ROLE_STUDENT).build();
        when(userRepo.findByUsername("existing")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/auth/register/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username already taken"));
    }

    @Test
    void registerTeacher_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("teacher1");
        req.setPassword("pass123");
        req.setFullName("Test Teacher");

        when(userRepo.findByUsername("teacher1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        mockMvc.perform(post("/api/auth/register/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("teacher registered"));
    }

    @Test
    void registerTeacher_duplicateUsername_returnsBadRequest() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass123");
        req.setFullName("Duplicate");

        User existingUser = User.builder()
                .id(1L).username("existing").password("enc").role(Role.ROLE_TEACHER).build();
        when(userRepo.findByUsername("existing")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/auth/register/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username already taken"));
    }

    // ─── Login Tests ────────────────────────────────────────────

    @Test
    void login_success_returnsToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("student1");
        req.setPassword("pass123");

        User user = User.builder()
                .id(1L).username("student1").password("enc").role(Role.ROLE_STUDENT).build();

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("student1", "pass123"));
        when(userRepo.findByUsername("student1")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("student1", "ROLE_STUDENT")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("wrong");
        req.setPassword("wrong");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("invalid credentials"));
    }
}
