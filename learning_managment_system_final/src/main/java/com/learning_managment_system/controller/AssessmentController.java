package com.learning_managment_system.controller;

import java.util.List;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.learning_managment_system.model.Assignment;
import com.learning_managment_system.model.Quiz;
import com.learning_managment_system.model.Submission;
import com.learning_managment_system.service.AssessmentService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    //Common functions
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    @GetMapping()
    public ResponseEntity<List<Map<String, Object>>> getAssessmentsByUser(Authentication authentication){
        return ResponseEntity.ok(assessmentService.getAssessmentsByUser(authentication.getName()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    @GetMapping("/course/{courseTitle}")
    public ResponseEntity<List<Map<String, Object>>> getAssessmentsByCourse(@PathVariable String courseTitle){
        return ResponseEntity.ok(assessmentService.getAssessmentsByCourse(courseTitle));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    @GetMapping("/{assessmentTitle}")
    public ResponseEntity<Map<String, Object>> getAssessment(@PathVariable String assessmentTitle){
        return ResponseEntity.ok(assessmentService.getAssessment(assessmentTitle));
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or principal.username == #username")
    @GetMapping("/grades/user/{username}")
    public List<Submission> getGradesByUser(@PathVariable String username ,Authentication authentication) {
        return assessmentService.getGradesByStudent(username);
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    @GetMapping("/grades/{assessmentTitle}")
    public List<Submission> getGradesByAssessment(@PathVariable String assessmentTitle) {
        return assessmentService.getGradesByAssessment(assessmentTitle);
    }

    @PreAuthorize("hasRole('INSTRUCTOR') or principal.username == #username")
    @GetMapping(value = "/get_feedback", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> getAssignmentFeedback(@RequestParam String assignmentTitle
                , @RequestParam String username) {
        return ResponseEntity.ok(assessmentService.getAssignmentFeedback(assignmentTitle, username));
    }
    

    //Instructor functions
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/assignment")
    public ResponseEntity<Assignment> createAssignment(@Valid @RequestBody Assignment assignment) {
        if(assignment.getFullMark() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"FullMark is required!");
        }
        return ResponseEntity.ok(assessmentService.createAssignment(assignment));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/quiz")
    public ResponseEntity<Quiz> createQuiz(@Valid @RequestBody Quiz quiz) throws BadRequestException {
        if(quiz.getType() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Questions Type required!");
        }
        if(quiz.getNQuestions() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Number of Questions required!");
        }
        if(quiz.getFullMark() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"FullMark is required!");
        }
        return ResponseEntity.ok(assessmentService
                .createQuiz(quiz, quiz.getType().toString(), quiz.getNQuestions()));
    }
    
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{assignmentTitle}/submissions")
    public ResponseEntity<Map<String,String>> getAssignmentSubmissions(@PathVariable String assignmentTitle) {
        return ResponseEntity.ok(assessmentService.getAssignmentSubmissions(assignmentTitle));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/{assessmentTitle}/mark")
    public ResponseEntity<String> gradeAssessment(@PathVariable String assessmentTitle
            , @RequestBody Submission submission) throws BadRequestException {// grade, feedback, studentName
        if(submission.getGrade() == null){
            throw new BadRequestException("Grade is required");
        }
        if(submission.getStudentName() == null){
            throw new BadRequestException("Student Name is required");
        }
        if(assessmentService.gradeAssessment(assessmentTitle, submission)){
            return ResponseEntity.ok("Assessment marked successfully");
        }
        else return ResponseEntity.badRequest().body("Could not mark assessment");
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{assessmentTitle}")
    public ResponseEntity<String> deleteAssessment(@PathVariable String assessmentTitle) {
        assessmentService.deleteAssessment(assessmentTitle);
        return ResponseEntity.ok("Assessment deleted successfully");
    }

    //Student functions
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{assignmentTitle}/submit_assignment")
    public ResponseEntity<String> submitAssignment(@PathVariable String assignmentTitle ,@RequestBody MultipartFile submissionFile, Authentication authentication) {
        String studentUsername =  authentication.getName();
        assessmentService.submitAssignment(assignmentTitle, studentUsername, submissionFile);
        return ResponseEntity.ok("Assignment submitted successfully");
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit_quiz")
    public ResponseEntity<String> submitQuiz(@Valid @RequestBody Quiz quiz, Authentication authentication) {
        String studentUsername =  authentication.getName();
        if(quiz.getQuestions() == null) throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No Answers submitted!");
        return ResponseEntity.ok("Quiz submitted successfully\nFeedback: " + assessmentService.submitQuiz(quiz, studentUsername));
    }
    
}
