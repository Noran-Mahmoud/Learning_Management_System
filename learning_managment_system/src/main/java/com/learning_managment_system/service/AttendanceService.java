package com.learning_managment_system.service;

import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.LessonAttendance;
import com.learning_managment_system.repository.LessonAttendanceRepository;
import com.learning_managment_system.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonAttendanceRepository lessonAttendanceRepository;


    public String generateOtpForLesson(Long lessonId) {
        Optional<Lesson> lesson = lessonRepository.findById(lessonId);
        if (lesson.isPresent()) {
            String otp = generateOtp();
            LessonAttendance attendance = new LessonAttendance();
            attendance.setLesson(lesson.get());
            attendance.setOtp(otp);
            attendance.setGeneratedAt(LocalDateTime.now());
            lessonAttendanceRepository.save(attendance);
            return otp;
        } else {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
    }

    public boolean validateOtp(Long lessonId, String otp) {
        Optional<LessonAttendance> attendance = lessonAttendanceRepository.findByLessonIdAndOtp(lessonId, otp);
        if (attendance.isPresent()) {
            LessonAttendance record = attendance.get();
            if (record.isValidated()) {
                return false;
            }


            record.setValidated(true);
            record.setValidatedAt(LocalDateTime.now());
            lessonAttendanceRepository.save(record);
            return true;
        } else {
            return false;
        }
    }



    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
