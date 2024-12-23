package com.learning_managment_system.controller;

import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.UserRepository;
import com.learning_managment_system.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/generate-otp")
    public ResponseEntity<String> generateOtp(@PathVariable Long lessonId, @RequestParam Long studentId) {
        String otp = attendanceService.generateOtpForLesson(lessonId, studentId);
        return ResponseEntity.ok(otp);
    }


@PreAuthorize("hasRole('STUDENT')")
@PostMapping("/{lessonId}/validate-otp")
public ResponseEntity<Boolean> validateOtp(
    @PathVariable Long lessonId,
    @RequestParam Long studentId,
    @RequestParam String otp,
    Authentication authentication) {

    String username = authentication.getName();
    Optional<User> userOpt = userRepository.findById(studentId);
    if (userOpt.isPresent()) {
        User user = userOpt.get();

        if (!username.equals(user.getUsername())) {
            throw new AccessDeniedException("You do not have permission to validate OTP for this student.");
        }

        boolean isValid = attendanceService.validateOtp(lessonId, studentId, otp);
        return ResponseEntity.ok(isValid);
    } else {
        throw new RuntimeException("Student not found");
    }
}


}
