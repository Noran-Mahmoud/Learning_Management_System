package com.learning_managment_system.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@Getter@Setter @AllArgsConstructor

@Entity

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String duration;
    private String mediaFileUrl;

    @ManyToOne @JoinColumn(name = "instructor_id", nullable = false)
    @JsonBackReference
    @NotNull
    private User instructor;

    @ManyToMany(mappedBy = "enrolledCourses")
    private List<User> enrolledStudents= new ArrayList<User>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Lesson> lessons;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Assessment> assessments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("course-question")
    private Set<Question> questionBank;

    public Course(Long id, String title, String description, String duration, String mediaFileUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.mediaFileUrl = mediaFileUrl;
    }
    public Course(){}

    public Course(String title, Long id, String description, String duration, String mediaFileUrl, User instructor, List<User> enrolledStudents, Set<Lesson> lessons, Set<Assessment> assessments, Set<Question> questionBank) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.duration = duration;
        this.mediaFileUrl = mediaFileUrl;
        this.instructor = instructor;
        this.enrolledStudents = enrolledStudents;
        this.lessons = lessons;
        this.assessments = assessments;
        this.questionBank = questionBank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMediaFileUrl() {
        return mediaFileUrl;
    }

    public void setMediaFileUrl(String mediaFileUrl) {
        this.mediaFileUrl = mediaFileUrl;
    }

    public @NotNull User getInstructor() {
        return instructor;
    }

    public void setInstructor(@NotNull User instructor) {
        this.instructor = instructor;
    }

    public List<User> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(List<User> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public Set<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(Set<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<Assessment> assessments) {
        this.assessments = assessments;
    }

    public Set<Question> getQuestionBank() {
        return questionBank;
    }

    public void setQuestionBank(Set<Question> questionBank) {
        this.questionBank = questionBank;
    }
}
