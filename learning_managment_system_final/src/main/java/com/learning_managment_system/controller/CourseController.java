package com.learning_managment_system.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.Question;
import com.learning_managment_system.service.CourseService;
import com.learning_managment_system.service.QuestionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final QuestionService questionService;

    public CourseController(CourseService courseService, QuestionService questionService) {
        this.courseService = courseService;
        this.questionService = questionService;
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getCourses() {
        List<Map<String, Object>> courses = courseService.getAllCourses();
        if(courses.isEmpty())
            throw new ResponseStatusException(HttpStatus.NO_CONTENT ,"No courses found");
        return ResponseEntity.ok(courses);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping()
    public ResponseEntity<Course> createCourse(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart("duration") String duration,
            @RequestPart("instructor") String instructorName,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile
    ) {

        Course savedCourse = courseService.createCourse(title, description, duration, instructorName, mediaFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableCoursesForUser(Authentication authentication) {
        List<Map<String, Object>> response = courseService.getAvailableCoursesForUser(authentication.getName());
        return ResponseEntity.ok(response);
    }
    // Enroll in a course
    @PostMapping("/{courseTitle}/enroll")
    @PreAuthorize("hasRole('STUDENT')") // Ensure only students can access
    public ResponseEntity<String> enrollInCourse(@PathVariable String courseTitle, Authentication authentication) {
        String username = authentication.getName();
        courseService.enrollInCourse(courseTitle, username);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @GetMapping("/{courseTitle}/students")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getEnrolledStudents(@PathVariable String courseTitle) {
        List<String> studentNames = courseService.getEnrolledStudentNames(courseTitle);
        return ResponseEntity.ok(studentNames);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/{courseTitle}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable String courseTitle,
            @RequestBody Course updatedCourse) {

        Map<String, Object> courseData = courseService.updateCourse(courseTitle, updatedCourse);

        return ResponseEntity.ok(courseData);
    }


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{courseTitle}")
    public ResponseEntity<String> deleteCourse(@PathVariable String courseTitle) {
        courseService.deleteCourse(courseTitle);
        return ResponseEntity.ok("Course deleted successfully");
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{courseTitle}/lessons")
    public ResponseEntity<Map<String, Object>> createLesson(
            @PathVariable String courseTitle,
            @Valid @RequestBody Lesson lesson){
        Course course = courseService.addLessonToCourse(courseTitle, lesson);
        Set <Lesson> lessons = course.getLessons();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lesson successfully added to " + course.getTitle());
        response.put("All lessons in course", lessons);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{courseTitle}/lessons")
    public ResponseEntity<Set<Lesson>> getLessonsByCourse(@PathVariable String courseTitle){
        return ResponseEntity.ok(courseService.getLessonsByCourse(courseTitle));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{courseTitle}/question_bank")
    public ResponseEntity<String> addToQuestionBank(@RequestBody List<@Valid Question> questions ,@PathVariable String courseTitle) {
        for(Question question: questions) {
            if(question.getCorrectAnswer() == null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Correct answer is required");
            if(question.getMarks() == null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Marks is required");
            if(question.getType() == null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Question type is required");
            if(question.getType().toString() == "MCQ" && question.getOptions() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Options are required for MCQ");

            questionService.createQuestion(question, courseTitle);
        }
        return ResponseEntity.ok("Question added successfully");
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/get/{courseTitle}/question_bank")
    public ResponseEntity<Set<Question>> getQuestionsOfCourse(@PathVariable String courseTitle){
        return ResponseEntity.ok(questionService.getQuestionsOfCourse(courseTitle));
    }

}
