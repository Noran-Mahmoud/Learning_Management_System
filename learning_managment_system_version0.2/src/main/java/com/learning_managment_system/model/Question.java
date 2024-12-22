package com.learning_managment_system.model;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
@Entity
public class Question {
    public Question(){}
    public Question(String text, String correctAnswer, Double marks, Type type) {
        this.text = text;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.type = type;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private String correctAnswer;
    @NotNull
    private Double marks;
    
    public enum Type{
        MCQ,
        T_F,
        SHORT_ANSWER
    };

    @Enumerated(EnumType.STRING)
    @NotNull
    private Type type;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    private Set<String> options;

    private Integer questionOrder;

    @ManyToOne @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference("course-question")
    @NotNull
    private Course course;

    @ManyToOne @JoinColumn(name = "quiz_id")
    @JsonBackReference("quiz-question")
    private Assessment quiz;
}
