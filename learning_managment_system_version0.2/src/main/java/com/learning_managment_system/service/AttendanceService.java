package com.learning_managment_system.service;


import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.LessonAttendance;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.LessonAttendanceRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class AttendanceService {

    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LessonAttendanceRepository lessonAttendanceRepository;

    public String generateOtpForLesson(Long lessonId, String studentName) {

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));


        User student = userRepository.findByUsername(studentName)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));


        boolean isStudentEnrolled = lesson.getCourse()
            .getEnrolledStudents()
            .stream()
            .anyMatch(s -> s.getUsername().equals(studentName));

        if (!isStudentEnrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }


        String otp = generateOtp();


        LessonAttendance attendance = new LessonAttendance();
        attendance.setLesson(lesson);
        attendance.setOtp(otp);
        attendance.setGeneratedAt(LocalDateTime.now());
        attendance.setValidated(false);
        attendance.setStudentId(student.getId());


        lessonAttendanceRepository.save(attendance);

        return otp;
    }

    public boolean validateOtp(Long lessonId, String studentName,Long studentId, String otp) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        boolean isStudentEnrolled = lesson.getCourse()
            .getEnrolledStudents()
            .stream()
            .anyMatch(student -> student.getUsername().equals(studentName));

        if (!isStudentEnrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        Optional<LessonAttendance> attendance = lessonAttendanceRepository
            .findByLessonIdAndStudentIdAndOtp(lessonId, studentId, otp);

        if (attendance.isPresent() && !attendance.get().isValidated()) {
            LessonAttendance record = attendance.get();
            record.setValidated(true);
            record.setValidatedAt(LocalDateTime.now());
            lessonAttendanceRepository.save(record);
            return true;
        }

        return false;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public List<String> getAttendancesForLesson(Long lessonId) {
        lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        List <LessonAttendance> attendances =  lessonAttendanceRepository.findByLessonIdAndValidatedTrue(lessonId);
        if(attendances.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No students attended");
        }
        List <Optional<User>> students = attendances.stream()
            .map(a -> userRepository.findById(a.getStudentId())).collect(Collectors.toList());
        List <String> names = students.stream().filter(s -> s.isPresent())
            .map(s -> s.get().getUsername()).collect(Collectors.toList());

        return names;
    }
}
