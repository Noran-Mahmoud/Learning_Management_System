package com.learning_managment_system.repository;

import com.learning_managment_system.model.Submission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmitRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssessmentId(Long assessmentId);
    List<Submission> findByStudentId(Long studentId);
    Optional<Submission> findByStudentIdAndAssessmentId(Long studentId, Long assessmentId);
    Optional<Submission> findByStudentIdAndAssessmentTitle(Long studentId, String assessmentTitle);
}
