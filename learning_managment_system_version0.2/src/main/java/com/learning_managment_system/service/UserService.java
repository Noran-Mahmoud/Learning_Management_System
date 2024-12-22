package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    @Autowired @Lazy
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CourseRepository courseRepository
            ,@Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    public String loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return "Login successful\n";
        }
        return "Invalid username or password\n";
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("phoneNumber", user.getPhoneNumber());
            return userData;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> updateUserProfile(String username, User updatedUser) {
        User existingUser = getUserByUsername(username);

        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", savedUser.getId());
        userData.put("username", savedUser.getUsername());
        userData.put("email", savedUser.getEmail());
        userData.put("role", savedUser.getRole());
        userData.put("firstName", savedUser.getFirstName());
        userData.put("lastName", savedUser.getLastName());
        userData.put("phoneNumber", savedUser.getPhoneNumber());
        return userData;
    }

    public Map<String, Object> getUserDataByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("phoneNumber", user.getPhoneNumber());

        return userData;
    }

    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        return org.springframework.security.core.userdetails.User
            .builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }
    
    public void removeStudentFromCourse(String studentName, String courseTitle) {
        User student = userRepository.findByUsername(studentName)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.getEnrolledStudents().remove(student);
        student.getEnrolledCourses().remove(course);
        courseRepository.save(course);
        userRepository.save(student);
    }
}
