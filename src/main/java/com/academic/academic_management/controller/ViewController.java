package com.academic.academic_management.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/student")
    public String studentDashboard() {
        return "student-dashboard";
    }

    @GetMapping("/teacher")
    public String teacherDashboard() {
        return "teacher-dashboard";
    }
}

