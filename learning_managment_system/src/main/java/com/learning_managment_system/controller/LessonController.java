package com.learning_managment_system.controller;



import com.learning_managment_system.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private AttendanceService attendanceService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/generate-otp")
    public ResponseEntity<String> generateOtp(@PathVariable Long lessonId) {
        String otp = attendanceService.generateOtpForLesson(lessonId);
        return ResponseEntity.ok(otp);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{lessonId}/validate-otp")
    public ResponseEntity<Boolean> validateOtp(@PathVariable Long lessonId, @RequestParam String otp) {
        boolean isValid = attendanceService.validateOtp(lessonId, otp);
        return ResponseEntity.ok(isValid);
    }
}
