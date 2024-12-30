package com.learning_managment_system.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.learning_managment_system.model.User;
import com.learning_managment_system.model.Assessment;
import com.learning_managment_system.model.Assignment;
import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.NotificationType;
import com.learning_managment_system.model.Submission;
import com.learning_managment_system.model.Quiz;
import com.learning_managment_system.model.Question;
import com.learning_managment_system.repository.AssessmentRepository;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.QuestionRepository;
import com.learning_managment_system.repository.SubmitRepository;
import com.learning_managment_system.repository.UserRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class AssessmentService {
    @Autowired
    private final QuestionService questionService;
    @Autowired
    private final CourseService courseService;
    private final SubmitRepository submitRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    @Autowired
    private final NotificationService notificationService;

    public AssessmentService(QuestionService questionService, SubmitRepository submitRepository,
            AssessmentRepository assessmentRepository, UserRepository userRepository,
            CourseRepository courseRepository, CourseService courseService,
            NotificationService notificationService, QuestionRepository questionRepository) {
        this.questionService = questionService;
        this.submitRepository = submitRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.notificationService = notificationService;
        this.questionRepository = questionRepository;
    }

    public Assignment createAssignment(Assignment assignment) {
        if (assessmentRepository.findByTitle(assignment.getTitle()).isPresent()) {
            throw new RuntimeException("Assignment title already exists");
        }

        Course course = courseRepository.findByTitle(assignment.getCourseTitle())
                .orElseThrow(() -> new RuntimeException("Invalid courseId"));

        assignment.setCourse(course);
        Assignment savedAssignment = assessmentRepository.save(assignment);

        // Notify students
        List<String> studentUsernames = course.getEnrolledStudents().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        notificationService.notifyUsers(studentUsernames,
                "A new assignment '" + assignment.getTitle() + "' has been created for the course '" + course.getTitle()
                        + "'");

        return savedAssignment;
    }

    public Quiz createQuiz(Quiz quiz, String type, int nQuestions) {
        if(assessmentRepository.findByTitle(quiz.getTitle()).isPresent())
            throw new RuntimeException("Quiz title already exist");
        Course course = courseRepository.findByTitle(quiz.getCourseTitle())
            .orElseThrow(() -> new RuntimeException("Invalid courseId"));

        try {
            Question.Type.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid question type");
        }

        quiz.setQuestions(questionService.getRandomizedQuestions(quiz, quiz.getCourseTitle(), type, nQuestions));
        quiz.setCourse(course);

        Quiz savedQuiz = assessmentRepository.save(quiz);
        
        List<String> studentUsernames = course.getEnrolledStudents().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        notificationService.notifyUsers(studentUsernames,
                "A new quiz '" + quiz.getTitle() + "' has been created for the course '" + course.getTitle() + "'");
        return savedQuiz;
    }

    public List<Map<String, Object>> getAssessmentsByUser(String userName) {
        User user = userRepository.findByUsername(userName).get();
        List <Course> courses;
        if(user.getRole().equals("INSTRUCTOR")){
            courses = courseRepository.findByInstructorId(user.getId());
        }
        else{
            courses = user.getEnrolledCourses();
        }
        List<Assessment> assessments = courses.stream()
                .map(course -> assessmentRepository.findByCourseId(course.getId()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return assessments.stream().map(assessment -> {
            Map<String, Object> assessData = new HashMap<>();
            assessData.put("id", assessment.getId());
            assessData.put("title", assessment.getTitle());
            assessData.put("Course", assessment.getCourseTitle());
            assessData.put("deadLine", assessment.getDeadLine());
            assessData.put("description", assessment.getDescription());
            if(user.getRole().equals("STUDENT")){
                if(submitRepository.findByStudentIdAndAssessmentTitle(
                    user.getId(), assessment.getTitle()).isPresent())
                    assessData.put("Submitted", true);
                else assessData.put("Submitted", false);
            }
            return assessData;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getAssessment(String assessmentTitle) {
        Assessment assessment = assessmentRepository.findByTitle(assessmentTitle)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", assessment.getId());
            courseData.put("title", assessment.getTitle());
            courseData.put("Course", assessment.getCourseTitle());
            courseData.put("deadLine", assessment.getDeadLine());
            courseData.put("description", assessment.getDescription());
            return courseData;
    }

    public List<Map<String, Object>> getAssessmentsByCourse(String courseTitle) {
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Course " + courseTitle + " not found"));
        List<Assessment> assessments = assessmentRepository.findByCourseId(course.getId());
        return assessments.stream().map(assessment -> {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", assessment.getId());
            courseData.put("title", assessment.getTitle());
            courseData.put("Course", assessment.getCourseTitle());
            courseData.put("deadLine", assessment.getDeadLine());
            courseData.put("description", assessment.getDescription());
            return courseData;
        }).collect(Collectors.toList());
    }

    public String getAssignmentFeedback(String assignmentTitle, String studentName) {
        Assessment assignment = assessmentRepository.findByTitle(assignmentTitle)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if(assignment.getSubmissions().stream().noneMatch(submission -> submission.getStudentName().equals(studentName))) {
            throw new RuntimeException("Student did not submit the assignment");
        }
        String feedback = assignment.getSubmissions().stream().filter(
                submission -> submission.getStudentName().equals(studentName))
                .findFirst().get().getFeedBack();

        if (feedback == null)
            throw new RuntimeException("No feedback yet");
        else
            return feedback;
    }

    @Transactional
    public void deleteAssessment(String assessmentTitle) {
        Assessment assessment = assessmentRepository.findByTitle(assessmentTitle)
            .orElseThrow(() -> new RuntimeException("Assessment does not exist"));
        Course course = assessment.getCourse();
        if(assessment instanceof Quiz){
            ((Quiz)assessment).getQuestions().stream().forEach(qu -> {
                qu.setQuiz(null);
                questionRepository.save(qu);
            });
        }
        course.getAssessments().remove(assessment);
        courseRepository.save(course);
        assessmentRepository.delete(assessment);
    }

    public boolean gradeAssessment(String assessmentTitle, Submission submission) {
        Assessment assessment = assessmentRepository.findByTitle(assessmentTitle)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
        String studentName = submission.getStudentName();
        User student = userRepository.findByUsername(studentName)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Submission currentSubmission = submitRepository
                .findByStudentIdAndAssessmentTitle(student.getId(), assessmentTitle)
                .orElseThrow(() -> new RuntimeException("Assessment is not submitted yet"));

        double grade = assessment.getFullMark() != null
                ? Math.min(submission.getGrade(), assessment.getFullMark())
                : submission.getGrade();

        currentSubmission.setGrade(grade);
        if (submission.getFeedBack() != null && assessment instanceof Assignment) {
            currentSubmission.setFeedBack(submission.getFeedBack());
        }

        submitRepository.save(currentSubmission);

        // Notify the student
        String message = "Your assessment '" + assessmentTitle + "' has been graded: " + grade + " points.";
        notificationService.createNotification(student.getId(), message, NotificationType.GRADED_ASSIGNMENT.name());

        return true;
    }

    public List<Submission> getGradesByAssessment(String assessmentTitle) {
        Assessment assessmen = assessmentRepository.findByTitle(assessmentTitle)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        List<Submission> submissions = submitRepository.findByAssessmentId(assessmen.getId()).stream()
                .filter(assessment -> assessment.getGrade() != null ? true : false)
                .collect(Collectors.toList());

        if (submissions.isEmpty()) {
            throw new RuntimeException("Not graded yet");
        } else {
            return submissions;
        }
    }

    public List<Submission> getGradesByStudent(String studentName) {
        User student = userRepository.findByUsername(studentName)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Submission> submissions = submitRepository.findByStudentId(student.getId()).stream()
                .filter(submission -> submission.getGrade() != null ? true : false)
                .collect(Collectors.toList());

        if (submissions.isEmpty()) {
            throw new RuntimeException("Not graded yet");
        } else {
            return submissions;
        }
    }

    public void submitAssignment(String assignmentTitle, String studentName, MultipartFile submissionFile) {
        Assessment assignment = assessmentRepository.findByTitle(assignmentTitle)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment not found"));
        Course course = courseRepository.findByTitle(assignment.getCourseTitle())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found"));

        User student = userRepository.findByUsername(studentName).get();
        if(course.getEnrolledStudents().isEmpty() || (!course.getEnrolledStudents().contains(student)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not enrolled in course");

        
        if (submitRepository.findByStudentIdAndAssessmentTitle(
                student.getId(), assignment.getTitle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.ALREADY_REPORTED, "Assingment is submitted once");
        }

        String submissionUrl = null;
        if (submissionFile != null && (!submissionFile.isEmpty())) {
            submissionUrl = courseService.uploadMediaFile(submissionFile);
        }

        Submission submission = submitRepository.save(new Submission(student, student.getUsername() ,assignment, submissionUrl));
        assignment.getSubmissions().add(submission);
        assessmentRepository.save(assignment);
    }

    public String submitQuiz(Quiz answeredQuiz, String studentName) {
        Assessment quiz = assessmentRepository.findByTitle(answeredQuiz.getTitle())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quiz " + answeredQuiz.getTitle() + " not found"));

        Course course = courseRepository.findByTitle(quiz.getCourseTitle())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found"));

        User student = userRepository.findByUsername(studentName).get();
        if(course.getEnrolledStudents().isEmpty() || (!course.getEnrolledStudents().contains(student)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not enrolled in course");
        
        if (submitRepository.findByStudentIdAndAssessmentTitle(
                student.getId(), quiz.getTitle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.ALREADY_REPORTED, "Quiz is submitted once");
        }

        List <Question> answers = answeredQuiz.getQuestions();
        //generate and return feedback
        List <Question> questions = ((Quiz) quiz).getQuestions();
        if(questions.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No questions");
        questions.stream().forEach(q -> {
            if(q.getQuestionOrder() == null) 
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A question does not have question order!");
            });
        questions.stream().sorted(Comparator.comparingInt(Question::getQuestionOrder))
            .collect(Collectors.toList());

        String feedBack = "";
        for (Question question : questions) {
            feedBack += "Question" + question.getQuestionOrder() + ": ";

            String answer = answers.stream().filter(an -> {
                if (an.getQuestionOrder() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A question does not have question order!");
                return an.getQuestionOrder() == question.getQuestionOrder();
            })
                    .findFirst().get().getText();
            if (answer == null) {
                feedBack += "No answer provided";
            } else if (question.getCorrectAnswer().toLowerCase().equals(answer.toLowerCase())) {
                feedBack += "Correct\n";
            } else {
                feedBack += "The correct answer is " + question.getCorrectAnswer() + "\n";
            }
        }

        Submission submission = submitRepository.save(new Submission(userRepository.findByUsername(studentName).get(), studentName, quiz));
        quiz.getSubmissions().add(submission);
        assessmentRepository.save(quiz);

        return feedBack;
    }

    public Map<String, String> getAssignmentSubmissions(String assignmentTitle) {
        if(assessmentRepository.findByTitle(assignmentTitle).get() instanceof Quiz)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quizs do not have file submissions");
        Map <String, String> submitData = new HashMap<>();
        ((Assignment) assessmentRepository.findByTitle(assignmentTitle)
                .orElseThrow(() -> new RuntimeException("Assignment not found")))
                .getSubmissions().stream()
                .forEach(submission ->
                    {if (submission.getSubmissionFileUrl() == null)
                        submitData.put(submission.getStudentName(), "Submitted without file!");
                    else submitData.put(submission.getStudentName(), submission.getSubmissionFileUrl());
                    return;
                });
        return submitData;
    }

}
