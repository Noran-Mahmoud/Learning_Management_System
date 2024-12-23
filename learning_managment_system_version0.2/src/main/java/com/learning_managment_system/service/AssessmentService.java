package com.learning_managment_system.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.learning_managment_system.model.User;
import com.learning_managment_system.model.Assessment;
import com.learning_managment_system.model.Assignment;
import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Submission;
import com.learning_managment_system.model.Quiz;
import com.learning_managment_system.model.Question;
import com.learning_managment_system.repository.AssessmentRepository;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.SubmitRepository;
import com.learning_managment_system.repository.UserRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class AssessmentService {
    private final QuestionService questionService;
    private final CourseService courseService;
    private final SubmitRepository submitRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AssessmentService(QuestionService questionService, SubmitRepository submitRepository,
            AssessmentRepository assessmentRepository, UserRepository userRepository,
            CourseRepository courseRepository, CourseService courseService) {
        this.questionService = questionService;
        this.submitRepository = submitRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
    }

    public Assignment createAssignment(Assignment assignment) {
        if(assessmentRepository.findByTitle(assignment.getTitle()).isPresent())
            throw new RuntimeException("Assigment title already exist");
        Course course = courseRepository.findByTitle(assignment.getCourseTitle())
            .orElseThrow(() -> new RuntimeException("Invalid courseId"));
        assignment.setCourse(course);
        return assessmentRepository.save(assignment);
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
        return assessmentRepository.save(quiz);
    }

    public List<Assessment> getAssessmentsByUser(String userName) {
        User user = userRepository.findByUsername(userName).get();
        List <Course> courses = courseRepository.findByInstructorId(user.getId());
        List <Assessment> assessments = courses.stream()
            .map(course -> assessmentRepository.findByCourseId(course.getId()))
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return assessments;
    }

    public Assessment getAssessment(String assessmentTitle) {
        return assessmentRepository.findByTitle(assessmentTitle)
            .orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    public List<Assessment> getAssessmentsByCourse(String courseTitle) {
        Course course = courseRepository.findByTitle(courseTitle)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Course " + courseTitle + " not found"));
        return assessmentRepository.findByCourseId(course.getId());
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

        if(feedback == null) throw new RuntimeException("No feedback yet");
        else return feedback;
    }

    public void deleteAssessment(String assessmentTitle) {
        Assessment assessment = assessmentRepository.findByTitle(assessmentTitle)
            .orElseThrow(() -> new RuntimeException("Assessment does not exist"));
        Course course = assessment.getCourse();
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
        if(!student.getRole().equals("STUDENT")) {
            throw new RuntimeException("User is not a student");
        }

        Submission currentSubmission = submitRepository.findByStudentIdAndAssessmentTitle(student.getId(), assessmentTitle)
            .orElseThrow(() -> new RuntimeException("Assessment is not submitted yet"));
        // if(currentSubmission.getGrade() != null) {
        //     throw new RuntimeException("Assessment is already graded");
        // }
        
        double grade;
        if(assessment.getFullMark() != null){
            grade = Math.min(submission.getGrade(), assessment.getFullMark());
        } else {
            grade = submission.getGrade();
        }
        currentSubmission.setGrade(grade);
        if(submission.getFeedBack() != null && assessment instanceof Assignment){
            currentSubmission.setFeedBack(submission.getFeedBack());
        }
        submitRepository.save(currentSubmission);
        return true;
    }

    public List <Submission> getGradesByAssessment(String assessmentTitle) {
        Assessment assessmen = assessmentRepository.findByTitle(assessmentTitle)
            .orElseThrow(() -> new RuntimeException("Assessment not found"));

        List <Submission> submissions = submitRepository.findByAssessmentId(assessmen.getId()).stream()
                .filter(assessment -> assessment.getGrade() != null ? true : false)
                .collect(Collectors.toList());

        if (submissions.isEmpty()) {
            throw new RuntimeException("Not graded yet");
        } else {
            return submissions;
        }
    }

    public List <Submission> getGradesByStudent(String studentName) {
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
        courseRepository.findByTitle(assignment.getCourseTitle())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found"));

        User student = userRepository.findByUsername(studentName).get();
        if(submitRepository.findByStudentIdAndAssessmentTitle(
            student.getId() ,assignment.getTitle()).isPresent()){
            throw new ResponseStatusException(HttpStatus.ALREADY_REPORTED, "Assingment is submitted");
        }

        String submissionUrl = null;
        if(submissionFile != null && !submissionFile.isEmpty()) {
            submissionUrl = courseService.uploadMediaFile(submissionFile);
        }

        Submission submission = submitRepository.save(new Submission(student, student.getUsername() ,assignment, submissionUrl));
        if(assignment.getSubmissions() == null ||assignment.getSubmissions().isEmpty())
            assignment.setSubmissions(new ArrayList<>());
        assignment.getSubmissions().add(submission);
        assessmentRepository.save(assignment);
    }

    public String submitQuiz(Quiz answeredQuiz, String studentName) {
        Assessment quiz = assessmentRepository.findByTitle(answeredQuiz.getTitle())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Quiz " + answeredQuiz.getTitle() + " not found"));

        List <Question> answers = answeredQuiz.getQuestions();
        //generate and return feedback
        List <Question> questions = ((Quiz) quiz).getQuestions().stream()
            .sorted(Comparator.comparingInt(Question::getQuestionOrder))
            .collect(Collectors.toList());
        String feedBack = "";
        for(Question question : questions) {
            feedBack += "Question" + question.getQuestionOrder() + ": ";

            String answer = answers.stream().filter(an -> {
                if(an.getQuestionOrder() == null) 
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A question does not have question order!");
                return an.getQuestionOrder() == question.getQuestionOrder();
            })
                    .findFirst().get().getText();
            if(answer == null) {
                feedBack += "No answer provided";
            } 
            else if(question.getCorrectAnswer().toLowerCase().equals(answer.toLowerCase())) {
                feedBack += "Correct\n";
            }
            else {
                feedBack += "The correct answer is " + question.getCorrectAnswer() + "\n";
            }
        }

        Submission submission = submitRepository.save(new Submission(userRepository.findByUsername(studentName).get(), studentName, quiz));
        if(quiz.getSubmissions() == null ||quiz.getSubmissions().isEmpty())
            quiz.setSubmissions(new ArrayList<>());
        quiz.getSubmissions().add(submission);
        assessmentRepository.save(quiz);

        return feedBack;
    }

    public List<String> getAssignmentSubmissions(String assignmentTitle) {
        return ((Assignment) assessmentRepository.findByTitle(assignmentTitle)
            .orElseThrow(() -> new RuntimeException("Assignment not found")))
            .getSubmissions().stream().map(submission -> {
                if(submission.getSubmissionFileUrl() == null)
                    return "Submitted without file!";
                return submission.getSubmissionFileUrl();
            }).collect(Collectors.toList());
    }

}
