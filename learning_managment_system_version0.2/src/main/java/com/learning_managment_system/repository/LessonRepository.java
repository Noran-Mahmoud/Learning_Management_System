package com.learning_managment_system.repository;

import com.learning_managment_system.model.Lesson;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByTitle(String title);
}
