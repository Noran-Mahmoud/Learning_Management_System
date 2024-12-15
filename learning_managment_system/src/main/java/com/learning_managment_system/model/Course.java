package com.learning_managment_system.model;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@Getter @Setter @RequiredArgsConstructor

public class Course {
    private final int courseID;
    private String courseTitle;
    private String description;
    private String duration;
    private List<Lesson> lessons;
    private List<Student> students;
    private List<Question> questionBank;
    private String mediaFileUrl;
}
