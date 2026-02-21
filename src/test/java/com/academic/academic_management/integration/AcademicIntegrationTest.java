package com.academic.academic_management.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academic.academic_management.dto.AuthRequest;
import com.academic.academic_management.dto.AuthResponse;
import com.academic.academic_management.dto.RegisterRequest;
import com.academic.academic_management.repository.CourseRepository;
import com.academic.academic_management.repository.StudentRepository;
import com.academic.academic_management.repository.TeacherRepository;
import com.academic.academic_management.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests that spin up the full Spring context with an H2 database.
 * Tests the complete flow: register → login → use authenticated endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AcademicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private CourseRepository courseRepo;

    private String studentToken;
    private String teacherToken;

    @BeforeAll
    void cleanDb() {
        courseRepo.deleteAll();
        studentRepo.deleteAll();
        teacherRepo.deleteAll();
        userRepo.deleteAll();
    }

    // ─── Student Registration & Login Flow ──────────────────────

    @Test
    @Order(1)
    void registerStudent_shouldReturn200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("teststudent");
        req.setPassword("pass123");
        req.setFullName("Test Student");

        mockMvc.perform(post("/api/auth/register/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("student registered"));
    }

    @Test
    @Order(2)
    void registerStudent_duplicate_shouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("teststudent");
        req.setPassword("pass123");
        req.setFullName("Test Student");

        mockMvc.perform(post("/api/auth/register/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username already taken"));
    }

    @Test
    @Order(3)
    void loginStudent_shouldReturnToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("teststudent");
        req.setPassword("pass123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        AuthResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        studentToken = resp.getToken();
    }

    @Test
    @Order(4)
    void getStudentProfile_withToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/students/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test Student"))
                .andExpect(jsonPath("$.username").value("teststudent"));
    }

    @Test
    @Order(5)
    void getStudentProfile_withoutToken_shouldReturn401or403() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden());
    }

    // ─── Teacher Registration & Login Flow ──────────────────────

    @Test
    @Order(6)
    void registerTeacher_shouldReturn200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testteacher");
        req.setPassword("pass123");
        req.setFullName("Test Teacher");

        mockMvc.perform(post("/api/auth/register/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("teacher registered"));
    }

    @Test
    @Order(7)
    void loginTeacher_shouldReturnToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("testteacher");
        req.setPassword("pass123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        AuthResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        teacherToken = resp.getToken();
    }

    @Test
    @Order(8)
    void getTeacherProfile_withToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/teachers/me")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test Teacher"))
                .andExpect(jsonPath("$.username").value("testteacher"));
    }

    // ─── Teacher Creates Course, Student Enrolls ────────────────

    @Test
    @Order(9)
    void teacherCreateCourse_shouldReturn200() throws Exception {
        String courseJson = """
                {
                    "title": "Data Structures",
                    "description": "Learn DS & Algo",
                    "code": "CS201"
                }
                """;

        mockMvc.perform(post("/api/teachers/me/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void listCourses_shouldReturnCreatedCourse() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Data Structures"))
                .andExpect(jsonPath("$[0].teacherName").value("Test Teacher"));
    }

    @Test
    @Order(11)
    void studentEnroll_shouldReturn200() throws Exception {
        // Course id = 1 (first created course)
        Long courseId = courseRepo.findAll().get(0).getId();

        mockMvc.perform(post("/api/students/me/enroll/" + courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(content().string("enrolled"));
    }

    @Test
    @Order(12)
    void studentProfile_shouldShowEnrolledCourse() throws Exception {
        mockMvc.perform(get("/api/students/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses[0].title").value("Data Structures"));
    }

    @Test
    @Order(13)
    void studentDrop_shouldReturn200() throws Exception {
        Long courseId = courseRepo.findAll().get(0).getId();

        mockMvc.perform(post("/api/students/me/drop/" + courseId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(content().string("dropped"));
    }

    // ─── Login Failure ──────────────────────────────────────────

    @Test
    @Order(14)
    void login_wrongPassword_shouldReturn401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("teststudent");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("invalid credentials"));
    }
}
