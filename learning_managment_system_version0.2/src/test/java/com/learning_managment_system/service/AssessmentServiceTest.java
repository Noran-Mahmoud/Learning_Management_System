package com.learning_managment_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.learning_managment_system.model.*;
import com.learning_managment_system.repository.*;

class AssessmentServiceTest {
    @Mock
    private QuestionService questionService;
    @Mock
    private CourseService courseService;
    @Mock
    private SubmitRepository submitRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private AssessmentService assessmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAssignment_Success() {
        Assignment assignment = new Assignment();
        assignment.setTitle("Math Assignment");
        assignment.setCourseTitle("Math 101");

        Course course = new Course();
        course.setTitle("Math 101");

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.empty());
        when(courseRepository.findByTitle("Math 101")).thenReturn(Optional.of(course));
        when(assessmentRepository.save(assignment)).thenReturn(assignment);

        Assignment result = assessmentService.createAssignment(assignment);

        assertEquals("Math Assignment", result.getTitle());
        assertEquals(course, result.getCourse());
        verify(assessmentRepository).save(assignment);
    }

    @Test
    void testCreateAssignment_TitleExists_ThrowsException() {
        Assignment assignment = new Assignment();
        assignment.setTitle("Math Assignment");

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assignment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assessmentService.createAssignment(assignment);
        });

        assertEquals("Assigment title already exist", exception.getMessage());
    }

    @Test
    void testCreateQuiz_Success() {
        Quiz quiz = new Quiz();
        quiz.setTitle("Java Quiz");
        quiz.setCourseTitle("Java 101");

        Course course = new Course();
        course.setTitle("Java 101");

        List<Question> mockQuestions = List.of(new Question(), new Question());

        when(assessmentRepository.findByTitle("Java Quiz")).thenReturn(Optional.empty());
        when(courseRepository.findByTitle("Java 101")).thenReturn(Optional.of(course));
        when(questionService.getRandomizedQuestions(quiz, "Java 101", "MCQ", 2)).thenReturn(mockQuestions);
        when(assessmentRepository.save(quiz)).thenReturn(quiz);

        Quiz result = assessmentService.createQuiz(quiz, "MCQ", 2);

        assertEquals("Java Quiz", result.getTitle());
        assertEquals(mockQuestions, result.getQuestions());
        assertEquals(course, result.getCourse());
    }

    @Test
    void testCreateQuiz_InvalidQuestionType_ThrowsException() {
    Quiz quiz = new Quiz();
    quiz.setTitle("Java Quiz");
    quiz.setCourseTitle("Java Course");

    Course course = new Course();
    course.setTitle("Java Course");
    when(courseRepository.findByTitle("Java Course")).thenReturn(Optional.of(course));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        assessmentService.createQuiz(quiz, "INVALID_TYPE", 2);
    });

    assertEquals("Invalid question type", exception.getMessage());

    verify(assessmentRepository, never()).save(any(Quiz.class));
}

    @Test
    void testGetAssessmentsByUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("instructor");

        Course course1 = new Course();
        course1.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);

        List<Course> mockCourses = List.of(course1, course2);

        Assessment assessment1 = new Assessment();
        assessment1.setTitle("Assessment 1");
        Assessment assessment2 = new Assessment();
        assessment2.setTitle("Assessment 2");

        when(userRepository.findByUsername("instructor")).thenReturn(Optional.of(user));
        when(courseRepository.findByInstructorId(1L)).thenReturn(mockCourses);
        when(assessmentRepository.findByCourseId(1L)).thenReturn(List.of(assessment1));
        when(assessmentRepository.findByCourseId(2L)).thenReturn(List.of(assessment2));

        List<Assessment> result = assessmentService.getAssessmentsByUser("instructor");

        assertEquals(2, result.size());
        assertTrue(result.stream().map(Assessment::getTitle).collect(Collectors.toList()).contains("Assessment 1"));
        assertTrue(result.stream().map(Assessment::getTitle).collect(Collectors.toList()).contains("Assessment 2"));
    }

    @Test
    void testGetAssignmentFeedback_Success() {
        Submission submission = new Submission();
        submission.setStudentName("student1");
        submission.setFeedBack("Great work!");

        Assessment assignment = new Assessment();
        assignment.setTitle("Math Assignment");
        assignment.setSubmissions(List.of(submission));

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assignment));

        String feedback = assessmentService.getAssignmentFeedback("Math Assignment", "student1");

        assertEquals("Great work!", feedback);
    }

    @Test
    void testGetAssignmentFeedback_NoSubmission_ThrowsException() {
        Assessment assignment = new Assessment();
        assignment.setTitle("Math Assignment");
        assignment.setSubmissions(List.of());

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assignment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assessmentService.getAssignmentFeedback("Math Assignment", "student1");
        });

        assertEquals("Student did not submit the assignment", exception.getMessage());
    }

    @Test
    void testDeleteAssessment_RemovedFromCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Course");

        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setTitle("Java Assignment");
        assessment.setCourse(course);

        course.setAssessments(new HashSet<>(Set.of(assessment)));

        when(assessmentRepository.findByTitle("Java Assignment")).thenReturn(Optional.of(assessment));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        doNothing().when(assessmentRepository).delete(assessment);

        assessmentService.deleteAssessment("Java Assignment");

        verify(assessmentRepository).delete(assessment);

        assertFalse(course.getAssessments().contains(assessment));
    }

    @Test
    void testDeleteAssessment_AssessmentNotFound_ThrowsException() {
        when(assessmentRepository.findByTitle("Nonexistent Assignment")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assessmentService.deleteAssessment("Nonexistent Assignment");
        });

        assertEquals("Assessment does not exist", exception.getMessage());
    }

    @Test
    void testSubmitAssignment_Success() {
        Assignment assignment = new Assignment();
        assignment.setTitle("Math Assignment");
        assignment.setCourseTitle("Math 101");
        assignment.setSubmissions(List.of());

        Course course = new Course();
        course.setTitle("Math 101");

        User student = new User();
        student.setId(1L);
        student.setUsername("student1");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assignment));
        when(courseRepository.findByTitle("Math 101")).thenReturn(Optional.of(course));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(courseService.uploadMediaFile(mockFile)).thenReturn("submission-url");

        Submission savedSubmission = new Submission(student, "student1", assignment, "submission-url");
        when(submitRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        assessmentService.submitAssignment("Math Assignment", "student1", mockFile);

        assertEquals(1, assignment.getSubmissions().size());
        assertEquals("submission-url", assignment.getSubmissions().get(0).getSubmissionFileUrl());
    }

    @Test
    void testSubmitAssignment_AlreadySubmitted_ThrowsException() {
        Assignment assignment = new Assignment();
        assignment.setTitle("Math Assignment");
        assignment.setCourseTitle("Math 101");

        Course course = new Course();
        course.setTitle("Math 101");

        User student = new User();
        student.setId(1L);
        student.setUsername("student1");

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assignment));
        when(courseRepository.findByTitle("Math 101")).thenReturn(Optional.of(course));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(submitRepository.findByStudentIdAndAssessmentTitle(1L, "Math Assignment")).thenReturn(Optional.of(new Submission()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            assessmentService.submitAssignment("Math Assignment", "student1", null);
        });

        assertEquals("208 ALREADY_REPORTED \"Assingment is submitted\"", exception.getMessage());
    }

    @Test
    void testGradeAssessment_Success() {
        Assessment assessment = new Assessment();
        assessment.setTitle("Math Assignment");
        assessment.setFullMark(100.0);

        User student = new User();
        student.setId(1L);
        student.setUsername("student1");
        student.setRole("STUDENT");

        Submission submission = new Submission(student, "student1", assessment, null);
        submission.setGrade(90.0);

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(submitRepository.findByStudentIdAndAssessmentTitle(1L, "Math Assignment")).thenReturn(Optional.of(submission));
        when(submitRepository.save(any(Submission.class))).thenReturn(submission);

        boolean result = assessmentService.gradeAssessment("Math Assignment", submission);

        assertTrue(result);
        assertEquals(90.0, submission.getGrade());
    }

    @Test
    void testGradeAssessment_StudentNotFound_ThrowsException() {
        Assessment assessment = new Assessment();
        assessment.setTitle("Math Assignment");

        Submission submission = new Submission();
        submission.setStudentName("student1");

        when(assessmentRepository.findByTitle("Math Assignment")).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assessmentService.gradeAssessment("Math Assignment", submission);
        });

        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    void testSubmitQuiz_Success() {
        Quiz quiz = new Quiz();
        quiz.setTitle("Java Quiz");

        Question question1 = new Question();
        question1.setQuestionOrder(1);
        question1.setCorrectAnswer("Programming Language");

        Question question2 = new Question();
        question2.setQuestionOrder(2);
        question2.setCorrectAnswer("Java Virtual Machine");

        quiz.setQuestions(List.of(question1, question2));

        Quiz answeredQuiz = new Quiz();
        answeredQuiz.setTitle("Java Quiz");

        Question answer1 = new Question();
        answer1.setQuestionOrder(1);
        answer1.setText("Programming Language");

        Question answer2 = new Question();
        answer2.setQuestionOrder(2);
        answer2.setText("Java Virtual Machine");

        answeredQuiz.setQuestions(List.of(answer1, answer2));

        User student = new User();
        student.setUsername("student1");

        when(assessmentRepository.findByTitle("Java Quiz")).thenReturn(Optional.of(quiz));
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));
        when(submitRepository.save(any(Submission.class))).thenReturn(new Submission(student, "student1", quiz));

        String feedback = assessmentService.submitQuiz(answeredQuiz, "student1");

        assertTrue(feedback.contains("Question1: Correct"));
        assertTrue(feedback.contains("Question2: Correct"));
    }

    @Test
    void testSubmitQuiz_InvalidAnswer_ThrowsException() {
        Quiz quiz = new Quiz();
        quiz.setTitle("Java Quiz");

        Question question1 = new Question();
        question1.setQuestionOrder(1);
        question1.setCorrectAnswer("Programming Language");

        quiz.setQuestions(List.of(question1));

        Quiz answeredQuiz = new Quiz();
        answeredQuiz.setTitle("Java Quiz");

        Question answer1 = new Question();
        answer1.setQuestionOrder(null); // No question order set

        answeredQuiz.setQuestions(List.of(answer1));

        when(assessmentRepository.findByTitle("Java Quiz")).thenReturn(Optional.of(quiz));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            assessmentService.submitQuiz(answeredQuiz, "student1");
        });

        assertEquals("400 BAD_REQUEST \"A question does not have question order!\"", exception.getMessage());
    }

}