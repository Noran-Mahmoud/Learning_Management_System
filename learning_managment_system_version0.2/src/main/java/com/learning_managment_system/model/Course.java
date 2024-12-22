package com.learning_managment_system.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
    private List<User> enrolledStudents;

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
}