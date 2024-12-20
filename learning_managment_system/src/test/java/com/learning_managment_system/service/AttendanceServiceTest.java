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

    private Lesson lesson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lesson = new Lesson();
        lesson.setId(1L);

    }

    @Test
    void testGenerateOtpForLesson_Success() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        String otp = attendanceService.generateOtpForLesson(1L);
        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(lessonAttendanceRepository, times(1)).save(any(LessonAttendance.class));
    }

    @Test
    void testGenerateOtpForLesson_LessonNotFound() {
        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateOtpForLesson(1L);
        });
        assertEquals("Lesson not found with id: 1", exception.getMessage());
    }

    @Test
    void testValidateOtp_Success() {
        LessonAttendance attendance = new LessonAttendance();
        attendance.setLesson(lesson);
        attendance.setOtp("456789");
        attendance.setGeneratedAt(LocalDateTime.now());

        when(lessonAttendanceRepository.findByLessonIdAndOtp(1L, "456789")).thenReturn(Optional.of(attendance));
        boolean result = attendanceService.validateOtp(1L, 1L, "456789");
        assertTrue(result);
        assertTrue(attendance.isValidated());
        verify(lessonAttendanceRepository, times(1)).save(attendance);
    }

    @Test
    void testValidateOtp_AlreadyValidated() {
        LessonAttendance attendance = new LessonAttendance();
        attendance.setLesson(lesson);
        attendance.setOtp("123456");
        attendance.setGeneratedAt(LocalDateTime.now());
        attendance.setValidated(true);

        when(lessonAttendanceRepository.findByLessonIdAndOtp(1L, "456789")).thenReturn(Optional.of(attendance));
        boolean result = attendanceService.validateOtp(1L, 1L, "456789");
        assertFalse(result);
        verify(lessonAttendanceRepository, never()).save(attendance);
    }

    @Test
    void testValidateOtp_Failure() {
        when(lessonAttendanceRepository.findByLessonIdAndOtp(1L, "456789")).thenReturn(Optional.empty());
        boolean result = attendanceService.validateOtp(1L, 1L, "456789");
        assertFalse(result);
    }
}
