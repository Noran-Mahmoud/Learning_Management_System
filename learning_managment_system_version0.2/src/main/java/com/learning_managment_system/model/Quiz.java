package com.learning_managment_system.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Entity
@DiscriminatorValue("QUIZ")

public class Quiz extends Assessment{
    public Quiz(){}
    public Quiz(String title, Double fullMark, String courseTitle, int nQuestions, Question.Type type) {
        super(title, fullMark, courseTitle);
        this.nQuestions = nQuestions;
        this.type = type;
    }

    @NotNull
    @Column(nullable = false, columnDefinition = "integer default 1")
    private int nQuestions;
    @NotNull @Column(nullable = false, columnDefinition = "varchar(50) default 'T_F'")
    private Question.Type type;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    @JsonManagedReference("quiz-question")
    private List<Question> questions;
}
