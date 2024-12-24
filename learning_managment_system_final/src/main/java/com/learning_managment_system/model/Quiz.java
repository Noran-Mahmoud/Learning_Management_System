package com.learning_managment_system.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@DiscriminatorValue("QUIZ")

public class Quiz extends Assessment{
    public Quiz(){}
    public Quiz(String title, Double fullMark, String courseTitle, Integer nQuestions, Question.Type type) {
        super(title, fullMark, courseTitle);
        this.nQuestions = nQuestions;
        this.type = type;
    }

    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer nQuestions;

    @Column(nullable = false, columnDefinition = "VARCHAR(50) default 'T_F'")
    @Enumerated(EnumType.STRING)
    private Question.Type type;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference("quiz-question")
    private List<Question> questions;
}
