package com.academic.academic_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.academic.academic_management.entity.Student;

public interface StudentRepository extends JpaRepository<Student,Long>{
Student findByUserId(Long userId);
}
