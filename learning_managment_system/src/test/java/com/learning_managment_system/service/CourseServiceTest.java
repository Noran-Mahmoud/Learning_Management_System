package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setTitle("intro to software");
        course.setDescription("content for begginers");
        course.setDuration("75");

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("what is software");
    }

    @Test
    void testCreateCourse() {
        when(courseRepository.save(course)).thenReturn(course);
        Course createdCourse = courseService.createCourse(course);
        assertNotNull(createdCourse);
        assertEquals(course.getTitle(), createdCourse.getTitle());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void testGetCoursesByInstructor() {
        when(courseRepository.findByInstructorId(1L)).thenReturn(List.of(course));
        var courses = courseService.getCoursesByInstructor(1L);
        assertNotNull(courses);
        assertFalse(courses.isEmpty());
        assertEquals(course.getTitle(), courses.get(0).getTitle());
        verify(courseRepository, times(1)).findByInstructorId(1L);
    }

    @Test
    void testUploadMediaFile_Success() throws IOException {
        when(file.getOriginalFilename()).thenReturn("media.mp4");
        when(file.getBytes()).thenReturn("dummy data".getBytes());
        Path path = Files.createTempFile("media", ".mp4");
        String filePath = courseService.uploadMediaFile(file);
        assertNotNull(filePath);
        assertTrue(filePath.contains("media_files"));
    }

    @Test
    void testUploadMediaFile_Failure() throws IOException {
        when(file.getOriginalFilename()).thenReturn("media.mp4");
        when(file.getBytes()).thenThrow(new IOException("File read error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> courseService.uploadMediaFile(file));
        assertEquals("Failed to upload file: File read error", exception.getMessage());
    }


    @Test
    void testUpdateCourse() {
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Updated Course");
        updatedCourse.setDescription("Updated Description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        Course result = courseService.updateCourse(1L, updatedCourse);
        assertNotNull(result);
        assertEquals("Updated Course", result.getTitle());
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Updated Course");

        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> courseService.updateCourse(1L, updatedCourse));
        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    void testDeleteCourse() {
        when(courseRepository.existsById(1L)).thenReturn(true);
        courseService.deleteCourse(1L);
        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCourse_CourseNotFound() {
        when(courseRepository.existsById(1L)).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> courseService.deleteCourse(1L));
        assertEquals("Course not found", exception.getMessage());
    }

}
