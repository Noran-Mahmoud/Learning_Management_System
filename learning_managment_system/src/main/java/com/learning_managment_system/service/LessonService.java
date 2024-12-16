package com.learning_managment_system.service;

import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    @Autowired
    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    // دالة لتوليد OTP للدرس
    public Lesson generateOtpForLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // توليد OTP عشوائي
        String otp = generateOtp();
        lesson.setOtp(otp);

        // تعيين الوقت الذي تم فيه توليد الـ OTP
        lesson.setOtpGeneratedAt(LocalDateTime.now());

        // حفظ التغييرات في الدرس
        lessonRepository.save(lesson);

        return lesson;
    }

    public String getOTPForLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return lesson.getOtp(); // إرجاع الـ OTP الخاص بالدرس
    }

    // دالة لتوليد OTP عشوائي مكون من 6 أرقام
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // توليد رقم عشوائي بين 100000 و 999999
        return String.valueOf(otp);
    }
}
