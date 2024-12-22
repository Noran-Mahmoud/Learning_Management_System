package com.learning_managment_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning_managment_system.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findById(int questionId);
}
