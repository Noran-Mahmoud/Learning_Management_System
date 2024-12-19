package com.learning_managment_system.controller;

import com.learning_managment_system.model.User;
import com.learning_managment_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Admin Only Can Create Users
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String username, @RequestParam String password) {
        String response = userService.loginUser(username, password);
        return ResponseEntity.ok(response);
    }
    // Get all users (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    // The account holder or admin only
    @PreAuthorize("hasRole('ADMIN') or principal.username == #username")
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Map<String, Object> user = userService.getUserDataByUsername(username);
        return ResponseEntity.ok(user);
    }
    // The account holder or admin only
    @PreAuthorize("hasRole('ADMIN') or principal.username == #username")
    @PatchMapping("/update/{username}")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable String username, @RequestBody User updatedUser) {

            Map<String, Object> updatedUserData = userService.updateUserProfile(username, updatedUser);
            return ResponseEntity.ok(updatedUserData);
    }


    // Delete a user (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping("/removeStudent/{studentId}/fromCourse/{courseId}")
    public ResponseEntity<String> removeStudentFromCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            userService.removeStudentFromCourse(studentId, courseId);
            return ResponseEntity.ok("Student removed from course successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

}
