package com.learning_managment_system.model;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@DiscriminatorValue("ASSIGNMENT")

public class Assignment extends Assessment {
    public Assignment(){}
    public Assignment(String title, Double fullMark, String courseTitle) {
        super(title, fullMark, courseTitle);
    }
}
