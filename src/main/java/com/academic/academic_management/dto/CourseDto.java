package com.academic.academic_management.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {

    private Long id;
    private String title;
    private String description;
    private String teacherName;
}