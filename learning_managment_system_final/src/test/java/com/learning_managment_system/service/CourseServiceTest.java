package com.learning_managment_system.service;



import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private CourseService courseService;
    @Mock
    private NotificationService notificationService;

    private Course course;
    private User instructor;

    @BeforeEach
    public void setUp() {

        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setDescription("Test Description");
        course.setDuration("2 hours");


        instructor = new User();
        instructor.setId(1L);
        instructor.setUsername("instructor1");

        course.setInstructor(instructor);


    }

    @Test
    public void testCreateCourse() {
        when(courseRepository.findByTitle(course.getTitle())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("instructor1")).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course createdCourse = courseService.createCourse("Test Course", "Test Description", "2 hours", "instructor1", null);

        assertNotNull(createdCourse);
        assertEquals("Test Course", createdCourse.getTitle());
        assertEquals("Test Description", createdCourse.getDescription());
        assertEquals("2 hours", createdCourse.getDuration());
    }

    @Test
    public void testCreateCourseWithExistingTitle() {
        when(courseRepository.findByTitle(course.getTitle())).thenReturn(Optional.of(course));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.createCourse("Test Course", "Test Description", "2 hours", "instructor1", null);
        });

        assertEquals("Course already exists", exception.getMessage());
    }

    @Test


    public void testEnrollInCourse() {

        User student = new User();
        student.setId(2L);
        student.setUsername("student1");

        student.setEnrolledCourses(new ArrayList<>());


        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(courseRepository.findByTitle("Test Course")).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);


        Course enrolledCourse = courseService.enrollInCourse("Test Course", "student1");


        assertNotNull(enrolledCourse);
        assertTrue(enrolledCourse.getEnrolledStudents().contains(student));
    }



@Test
public void testEnrollInCourseWhenUserAlreadyEnrolled() {

    User user = new User();
    user.setUsername("student1");
    user.setEnrolledCourses(new ArrayList<>());

    Course course = new Course();
    course.setTitle("Test Course");


    user.getEnrolledCourses().add(course);


    when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
    when(courseRepository.findByTitle("Test Course")).thenReturn(Optional.of(course));


    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
        courseService.enrollInCourse("Test Course", "student1");
    });


    assertEquals("You are already enrolled in this course", thrown.getMessage());
}


}

