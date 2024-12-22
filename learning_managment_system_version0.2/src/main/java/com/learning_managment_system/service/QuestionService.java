package com.learning_managment_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.stereotype.Service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Question;
import com.learning_managment_system.model.Question.Type;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.QuestionRepository;

import lombok.AllArgsConstructor;
@AllArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    public Question createQuestion(Question question, String courseTitle) {
        courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if(question.getCourse().getTitle()!= courseTitle) {
            throw new RuntimeException("Invalid course Id in question");
        }

        if(question.getType().toString() == "MCQ" && question.getOptions().contains(question.getCorrectAnswer())){
            throw new RuntimeException("Correct answer not in options");
        }
        courseRepository.findByTitle(courseTitle).get().getQuestionBank().add(question);
        return questionRepository.save(question);
    }

    public List<Question> getRandomizedQuestions(Long courseId, String type, int nQuestions) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Type questionType;
        try {
            questionType = Type.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid question type");
        }

        Set<Question> questions = course.getQuestionBank().stream()
                .filter(question -> question.getType().equals(questionType))
                .collect(Collectors.toSet());
        if (questions.size() < nQuestions) {
            throw new RuntimeException("Not enough questions");
        }
        List<Question> randomizedQuestions = new ArrayList<>(questions);
        Collections.shuffle(randomizedQuestions);

        for (int i = 0; i < randomizedQuestions.size(); i++) {
            randomizedQuestions.get(i).setQuestionOrder(i+1);
        }
        return new ArrayList<>(randomizedQuestions.subList(0, nQuestions));
    }

    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId).get();
        Course course = question.getCourse();
        course.getQuestionBank().remove(question); 
        courseRepository.save(course); 
    }
}
