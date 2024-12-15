package com.learning_managment_system.model;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@Getter @Setter 
@RequiredArgsConstructor

public class Lesson {
    private final int lessonID;
    private String lessonTitle;
    private String description;
    private String duration;
    private String OTP;
    private Map<Student, Boolean> attendanceRecords; 
}
