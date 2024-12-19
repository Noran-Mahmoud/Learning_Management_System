package com.learning_managment_system.service;

import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("Yasmeen");
        user.setPassword("2020");
        user.setFirstName("yasmenn");
        user.setLastName("Yahia");
        user.setPhoneNumber("123456789");
        user.setEmail("yasmenn@gmail.com");
        user.setRole("USER");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUser(user);


        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterUser_UsernameAlreadyTaken() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("Username is already taken", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        String result = userService.loginUser(user.getUsername(), user.getPassword());

        assertEquals("Login successful", result);
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.loginUser(user.getUsername(), "wrongPassword"));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testGetUserByUsername() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername(user.getUsername());


        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void testGetUserByUsername_UserNotFound() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserByUsername(user.getUsername()));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteUser() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getUsername());

        verify(userRepository, times(1)).delete(user);
    }
    @Test
    void testLoadUserByUsername() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));


        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(user.getUsername()));
    }

     @Test
    void testUpdateUserProfile_Success() {

        User existingUser = new User();
        existingUser.setUsername("Yasmeen");
        existingUser.setFirstName("yasmeen");
        existingUser.setLastName("Yahia");
        existingUser.setPhoneNumber("123456789");
        existingUser.setEmail("yasmeen@gmail.com");

        User updatedUser = new User();
        updatedUser.setFirstName("Yasmine");
        updatedUser.setLastName("Yahya");
        updatedUser.setPhoneNumber("1234");
        updatedUser.setEmail("yasminey@gmail.com");
        when(userRepository.findByUsername("Yasmeen")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        User result = userService.updateUserProfile("Yasmeen", updatedUser);

        assertNotNull(result);
        assertEquals("Yasmine", result.getFirstName());
        assertEquals("Yahya", result.getLastName());
        assertEquals("1234", result.getPhoneNumber());
        assertEquals("yasminey@gmail.com", result.getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }


}

