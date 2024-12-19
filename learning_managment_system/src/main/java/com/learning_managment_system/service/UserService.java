package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public UserService(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
        return userRepository.save(user);
    }

    public String loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return "Login successful";
        }
        throw new RuntimeException("Invalid username or password");
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


    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        userRepository.delete(user);
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
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));



        return org.springframework.security.core.userdetails.User
            .builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }
    public void removeStudentFromCourse(Long studentId, Long courseId) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));


        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));


        course.getEnrolledStudents().remove(student);
        student.getEnrolledCourses().remove(course);


        courseRepository.save(course);
        userRepository.save(student);
    }

}
