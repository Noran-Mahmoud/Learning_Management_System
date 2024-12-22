package com.learning_managment_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning_managment_system.model.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findById(long assessmentId);
    Optional<Assessment> findByTitle(String assignmentTitle);
    List<Assessment> findByCourseId(Long courseId);
}
