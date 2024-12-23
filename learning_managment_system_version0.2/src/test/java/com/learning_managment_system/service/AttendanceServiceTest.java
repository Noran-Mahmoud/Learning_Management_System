

package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.LessonAttendance;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonAttendanceRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

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

        Course course = new Course();
        course.setId(1L);

        User enrolledStudent = new User();
        enrolledStudent.setId(studentId);

        course.setEnrolledStudents(Collections.singletonList(enrolledStudent));
        lesson.setCourse(course);


        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        User foundStudent = new User();
        foundStudent.setId(studentId);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(foundStudent));
        when(courseRepository.findByIdWithEnrolledStudents(course.getId())).thenReturn(Optional.of(course));


        String otp = attendanceService.generateOtpForLesson(lessonId, studentId);


        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(lessonAttendanceRepository, times(1)).save(any(LessonAttendance.class));
    }

    @Test
    void testGenerateOtpForLesson_StudentNotEnrolled() {

        Long lessonId = 1L;
        Long studentId = 2L;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        Course course = new Course();
        course.setId(1L);
        course.setEnrolledStudents(Collections.emptyList());

        lesson.setCourse(course);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(new User()));
        when(courseRepository.findByIdWithEnrolledStudents(course.getId())).thenReturn(Optional.of(course));
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.generateOtpForLesson(lessonId, studentId);
        });

        verify(lessonAttendanceRepository, never()).save(any(LessonAttendance.class));
    }

    @Test
    void testValidateOtp_Success() {
        Long lessonId = 1L;
        Long studentId = 2L;
        String otp = "123456";


        Lesson lesson = new Lesson();
        lesson.setId(lessonId);


        Course course = new Course();
        course.setId(1L);

        User enrolledStudent = new User();
        enrolledStudent.setId(studentId);
        course.setEnrolledStudents(Collections.singletonList(enrolledStudent));

        lesson.setCourse(course);


        LessonAttendance attendance = new LessonAttendance();
        attendance.setOtp(otp);
        attendance.setValidated(false);
        attendance.setStudentId(studentId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseRepository.findByIdWithEnrolledStudents(course.getId())).thenReturn(Optional.of(course));
        when(lessonAttendanceRepository.findByLessonIdAndStudentIdAndOtp(lessonId, studentId, otp))
                .thenReturn(Optional.of(attendance));


        System.out.println("Lesson ID: " + lesson.getId());
        System.out.println("Student ID: " + studentId);
        System.out.println("Course ID: " + course.getId());
        System.out.println("OTP: " + otp);


        boolean result = attendanceService.validateOtp(lessonId, studentId, otp);


        assertTrue(result);
        verify(lessonAttendanceRepository, times(1)).save(attendance);
    }

    @Test
    void testValidateOtp_InvalidOtp() {

        Long lessonId = 1L;
        Long studentId = 2L;
        String otp = "123456";

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);


        Course course = new Course();
        course.setId(1L);

        User enrolledStudent = new User();
        enrolledStudent.setId(studentId);
        course.setEnrolledStudents(Collections.singletonList(enrolledStudent));

        lesson.setCourse(course);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseRepository.findByIdWithEnrolledStudents(course.getId())).thenReturn(Optional.of(course));
        when(lessonAttendanceRepository.findByLessonIdAndStudentIdAndOtp(lessonId, studentId, otp))
                .thenReturn(Optional.empty());

        System.out.println("Lesson ID: " + lesson.getId());
        System.out.println("Student ID: " + studentId);
        System.out.println("Course ID: " + course.getId());
        System.out.println("OTP: " + otp);

        boolean result = attendanceService.validateOtp(lessonId, studentId, otp);


        assertFalse(result);
        verify(lessonAttendanceRepository, never()).save(any(LessonAttendance.class));
    }

}
