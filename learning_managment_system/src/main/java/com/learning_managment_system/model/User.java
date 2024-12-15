package com.learning_managment_system.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter@Setter
@RequiredArgsConstructor
public class User {
    private final int userID;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
