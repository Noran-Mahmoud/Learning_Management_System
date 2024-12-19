package com.learning_managment_system.repository;
import java.util.List;
import com.learning_managment_system.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByTitleContainingIgnoreCase(String title);

}

