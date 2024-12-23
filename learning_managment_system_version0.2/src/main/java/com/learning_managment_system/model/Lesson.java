package com.learning_managment_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;



@Entity
@Getter@Setter
public class Lesson {
   public Lesson(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Column(nullable = false)
    private String title;
    private String content;



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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
