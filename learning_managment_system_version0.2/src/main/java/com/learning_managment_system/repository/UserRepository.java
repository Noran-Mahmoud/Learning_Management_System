package com.learning_managment_system.repository;

import com.learning_managment_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // static Optional<User> findByUsername(String username) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'findByUsername'");
    // }
    Optional<User> findByUsername(String username);
}
