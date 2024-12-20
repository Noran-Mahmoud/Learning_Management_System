package com.learning_managment_system.service;

import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.LessonAttendance;
import com.learning_managment_system.repository.LessonAttendanceRepository;
import com.learning_managment_system.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonAttendanceRepository lessonAttendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateOtpForLesson_Success() {

        Long lessonId = 1L;
        Long studentId = 2L;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        String otp = attendanceService.generateOtpForLesson(lessonId, studentId);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(lessonAttendanceRepository, times(1)).save(any(LessonAttendance.class));
    }

    @Test
    void testGenerateOtpForLesson_LessonNotFound() {
        Long lessonId = 1L;
        Long studentId = 2L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateOtpForLesson(lessonId, studentId);
        });
        assertEquals("Lesson not found", exception.getMessage());
        verify(lessonAttendanceRepository, never()).save(any(LessonAttendance.class));
    }

    @Test
    void testValidateOtp_Success() {
        Long lessonId = 1L;
        Long studentId = 2L;
        String otp = "112020";
        LessonAttendance attendance = new LessonAttendance();
        attendance.setOtp(otp);
        attendance.setValidated(false);

        when(lessonAttendanceRepository.findByLessonIdAndStudentIdAndOtp(lessonId, studentId, otp))
                .thenReturn(Optional.of(attendance));

        boolean isValid = attendanceService.validateOtp(lessonId, studentId, otp);
        assertTrue(isValid);
        assertTrue(attendance.isValidated());
        assertNotNull(attendance.getValidatedAt());
        verify(lessonAttendanceRepository, times(1)).save(attendance);
    }

    @Test
    void testValidateOtp_Failure() {
        Long lessonId = 1L;
        Long studentId = 2L;
        String otp = "112020";

        when(lessonAttendanceRepository.findByLessonIdAndStudentIdAndOtp(lessonId, studentId, otp))
                .thenReturn(Optional.empty());
        boolean isValid = attendanceService.validateOtp(lessonId, studentId, otp);
        assertFalse(isValid);
        verify(lessonAttendanceRepository, never()).save(any(LessonAttendance.class));
    }
}
