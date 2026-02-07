package com.academic.academic_management.dto;


import lombok.*;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDto {
    private Long id;
    private String fullName;
    private String username;
    private Set<CourseDto> courses;
}