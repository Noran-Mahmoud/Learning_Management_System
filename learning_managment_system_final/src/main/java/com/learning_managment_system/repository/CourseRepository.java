package com.learning_managment_system.repository;
import java.util.List;
import java.util.Optional;

import com.learning_managment_system.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByTitleContainingIgnoreCase(String title);
    Optional <Course> findByTitle(String title);

}

