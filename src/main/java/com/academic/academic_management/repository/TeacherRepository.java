package com.academic.academic_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.academic.academic_management.entity.Teacher;

public interface TeacherRepository extends  JpaRepository<Teacher,Long> {
Teacher findByUserId(Long userId);
}
