package com.learning_managment_system.service;

import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

public User updateUserProfile(String username, User updatedUser) {
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

    return userRepository.save(existingUser);
}

    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        userRepository.delete(user);
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
}
