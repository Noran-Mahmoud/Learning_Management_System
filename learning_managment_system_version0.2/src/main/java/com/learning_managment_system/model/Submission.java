package com.learning_managment_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Submissions")
@Getter@Setter
public class Submission {
    public Submission(){}
    public Submission(User student, String studentName, Assessment assessment) {
        this.student = student;
        this.studentName = studentName;
        this.assessment = assessment;
    }
    public Submission(User student, String studentName, Assessment assessment, String submissionFileUrl) {
        this.student = student;
        this.studentName = studentName;
        this.assessment = assessment;
        this.submissionFileUrl = submissionFileUrl;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double grade;
    private String feedBack;
    private String submissionFileUrl;

    @ManyToOne @JoinColumn(name = "student_id", nullable = false)
    @JsonBackReference("user-submission")
    private User student;

    private String studentName;

    @ManyToOne @JoinColumn(name = "assessment_id" , nullable = false)
    @JsonBackReference("assessment-submission")
    private Assessment assessment;
}
