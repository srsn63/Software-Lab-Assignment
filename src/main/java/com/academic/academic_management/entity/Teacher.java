package com.academic.academic_management.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "teachers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private Set<Course> courses;
}
