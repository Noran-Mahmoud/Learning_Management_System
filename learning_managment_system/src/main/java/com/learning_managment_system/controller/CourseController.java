package com.learning_managment_system.controller;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

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

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Course>> getCoursesByInstructor(@PathVariable Long instructorId) {
        List<Course> courses = courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{courseId}/lessons")
    public ResponseEntity<Lesson> createLesson(
            @PathVariable Long courseId,
            @RequestBody Lesson lesson
    ) {
        Lesson createdLesson = courseService.addLessonToCourse(courseId, lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }
}
