package com.learning_managment_system.repository;

import com.learning_managment_system.model.LessonAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonAttendanceRepository extends JpaRepository<LessonAttendance, Long> {

    Optional<LessonAttendance> findByLessonIdAndStudentIdAndOtp(Long lessonId, Long studentId, String otp);
    List<LessonAttendance> findByLessonIdAndValidatedTrue(Long lessonId);

}
