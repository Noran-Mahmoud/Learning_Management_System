package com.learning_managment_system.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String duration;
    private String mediaFileUrl;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;
    @ManyToMany(mappedBy = "enrolledCourses")
    private List<User> enrolledStudents;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Lesson> lessons;  
}
