package com.learning_managment_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter@Setter@AllArgsConstructor
public class Lesson {
    Lesson(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Column(nullable = false)
    private String title;
    private String content;

    private String otp;
    private LocalDateTime otpGeneratedAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonBackReference
    private Course course;

    public Lesson(Long id, String title, String content, Course course) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.course = course;
    }

}
