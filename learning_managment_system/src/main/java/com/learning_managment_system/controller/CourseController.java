package com.learning_managment_system.controller;
import com.learning_managment_system.dto.CourseDTO;
import com.learning_managment_system.dto.CourseEnrollmentDTO;
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


    @GetMapping("/all")
    public ResponseEntity<List<CourseDTO>> getCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')") // Restrict to Admins and Instructors
    public ResponseEntity<CourseEnrollmentDTO> getEnrolledStudents(@PathVariable Long courseId) {
        CourseEnrollmentDTO courseEnrollment = courseService.getEnrolledStudents(courseId);
        return ResponseEntity.ok(courseEnrollment);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructor(@PathVariable Long instructorId) {
        List<CourseDTO> courseDTOs = courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(courseDTOs);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
        @PathVariable Long courseId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "duration", required = false) String duration,
        @RequestParam(value = "mediaFileUrl", required = false) String mediaFileUrl
    ) {

        Course partialUpdate = new Course();
        partialUpdate.setTitle(title);
        partialUpdate.setDescription(description);
        partialUpdate.setDuration(duration);
        partialUpdate.setMediaFileUrl(mediaFileUrl);


        CourseDTO updatedCourse = courseService.updateCourse(courseId, partialUpdate);
        return ResponseEntity.ok(updatedCourse);
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
