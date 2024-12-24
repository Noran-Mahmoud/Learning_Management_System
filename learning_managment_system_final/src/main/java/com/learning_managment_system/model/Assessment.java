package com.learning_managment_system.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "assessment_type", discriminatorType = DiscriminatorType.STRING)
public class Assessment {

    public Assessment(){}
    public Assessment(String title, Double fullMark, String courseTitle) {
        this.title = title;
        this.fullMark = fullMark;
        this.courseTitle = courseTitle;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull @Column(nullable = false)
    private String title;

    @ManyToOne @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    private Course course;

    @NotNull
    private String courseTitle;
    
    private Double fullMark;
    private String deadLine;
    private String description;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference("assessment-submission")
    private List<Submission> submissions = new ArrayList<>();
}
