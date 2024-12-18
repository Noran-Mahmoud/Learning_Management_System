package com.learning_managment_system.dto;

import java.util.List;

public class CourseEnrollmentDTO {

    private String courseTitle;
    private List<String> enrolledStudents;

    // Constructor
    public CourseEnrollmentDTO(String courseTitle, List<String> enrolledStudents) {
        this.courseTitle = courseTitle;
        this.enrolledStudents = enrolledStudents;
    }

    // Getters and Setters
    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public List<String> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(List<String> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
}
