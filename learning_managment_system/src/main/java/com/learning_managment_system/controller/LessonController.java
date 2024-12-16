package com.learning_managment_system.controller;

import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    // توليد OTP لدرس معين
    @PostMapping("/{lessonId}/generate-otp")
    public ResponseEntity<String> generateOTP(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.generateOtpForLesson(lessonId);
        String otpDetails = String.format("OTP Generated: %s at %s", lesson.getOtp(), lesson.getOtpGeneratedAt());
        return ResponseEntity.ok(otpDetails);  // تضمين الوقت في الاستجابة
    }


    // استرجاع OTP لدرس معين
    @GetMapping("/{lessonId}/otp")
    public ResponseEntity<String> getOTP(@PathVariable Long lessonId) {
        String otp = lessonService.getOTPForLesson(lessonId);
        return ResponseEntity.ok("Current OTP: " + otp);
    }
}
