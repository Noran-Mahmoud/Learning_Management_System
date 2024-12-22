package com.learning_managment_system.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Course> createCourse(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("duration") String duration,
            @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile
    ) {
        String mediaFileUrl = null;
        if (mediaFile != null && !mediaFile.isEmpty()) {
            mediaFileUrl = courseService.uploadMediaFile(mediaFile);
        }

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setDuration(duration);
        course.setMediaFileUrl(mediaFileUrl);

        Course savedCourse = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }



    @GetMapping("/available/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableCoursesForStudent(@PathVariable Long studentId) {
        List<Map<String, Object>> response = courseService.getAvailableCoursesForStudent(studentId);
        return ResponseEntity.ok(response);
    }
        // Enroll in a course
        @PostMapping("/{courseId}/enroll")
        @PreAuthorize("hasRole('STUDENT')") // Ensure only students can access
        public ResponseEntity<String> enrollInCourse(@PathVariable Long courseId, Authentication authentication) {
            String username = authentication.getName();
            courseService.enrollInCourse(courseId, username);
            return ResponseEntity.ok("Enrolled successfully");
        }
    
    @GetMapping("/{courseId}/students")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<String>> getEnrolledStudents(@PathVariable Long courseId) {
        List<String> studentNames = courseService.getEnrolledStudentNames(courseId);
        return ResponseEntity.ok(studentNames);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/{courseId}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long courseId,
            @RequestBody Course updatedCourse) {

        Map<String, Object> courseData = courseService.updateCourse(courseId, updatedCourse);

        return ResponseEntity.ok(courseData);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{courseId}/lessons")
    public ResponseEntity<Lesson> createLesson(
            @PathVariable Long courseId,
            @RequestBody Lesson lesson
    ) {
        Lesson createdLesson = courseService.addLessonToCourse(courseId, lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

}
