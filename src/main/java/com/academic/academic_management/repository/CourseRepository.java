package com.academic.academic_management.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.academic.academic_management.entity.Course;
import java.util.List;

public interface CourseRepository extends  JpaRepository<Course,Long> {
    List<Course> findByTeacherId(Long teacherId);
}
