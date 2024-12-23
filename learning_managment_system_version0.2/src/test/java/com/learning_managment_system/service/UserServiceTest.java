package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        User user = new User();
        user.setUsername("yasmeen");
        user.setPassword("1810");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1810")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        Boolean result = userService.registerUser(user);

        assertTrue(result);
        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterUser_Failure_UserExists() {
        User user = new User();
        user.setUsername("yasmeen");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.of(user));

        Boolean result = userService.registerUser(user);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        User user = new User();
        user.setUsername("yasmeen");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1810", "encodedPassword")).thenReturn(true);

        String result = userService.loginUser("yasmeen", "1810");

        assertEquals("Login successful\n", result);
    }

    @Test
    void testLoginUser_Failure_InvalidPassword() {
        User user = new User();
        user.setUsername("yasmeen");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        String result = userService.loginUser("yasmeen", "wrongPassword");

        assertEquals("Invalid Password\n", result);
    }

    @Test
    void testLoginUser_Failure_InvalidUsername() {
        when(userRepository.findByUsername("invalidUser")).thenReturn(Optional.empty());

        String result = userService.loginUser("invalidUser", "1810");

        assertEquals("Invalid Username", result);
    }

    @Test
    void testUpdateUserProfile() {
        User existingUser = new User();
        existingUser.setUsername("yasmeen");
        existingUser.setEmail("yasmeen@email.com");

        User updatedUser = new User();
        updatedUser.setEmail("yasmeen1@email.com");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        Map<String, Object> result = userService.updateUserProfile("yasmeen", updatedUser);

        assertEquals("yasmeen1@email.com", result.get("email"));
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setUsername("yasmeen");

        when(userRepository.findByUsername("yasmeen")).thenReturn(Optional.of(user));

        userService.deleteUser("yasmeen");

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testRemoveStudentFromCourse() {
        User student = new User();
        student.setUsername("student1");
        Course course = new Course();
        course.setTitle("Java Course");

        List<User> enrolledStudents = new ArrayList<>();
        enrolledStudents.add(student);
        List<Course> enrolledCourses = new ArrayList<>();
        enrolledCourses.add(course);

        course.setEnrolledStudents(enrolledStudents);
        student.setEnrolledCourses(enrolledCourses);

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(courseRepository.findByTitle("Java Course")).thenReturn(Optional.of(course));

        userService.removeStudentFromCourse("student1", "Java Course");

        assertFalse(course.getEnrolledStudents().contains(student));
        assertFalse(student.getEnrolledCourses().contains(course));
        verify(courseRepository, times(1)).save(course);
        verify(userRepository, times(1)).save(student);
    }
}
