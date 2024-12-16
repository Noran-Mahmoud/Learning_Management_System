package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    // إنشاء دورة جديدة
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    // جلب الدورات الخاصة بمدرس معين
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    // رفع ملفات الوسائط
    public String uploadMediaFile(MultipartFile file) {
        try {
            String uploadDir = "media_files/";
            Path filePath = Paths.get(uploadDir + file.getOriginalFilename());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    // إضافة درس إلى دورة
    public Lesson addLessonToCourse(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        lesson.setCourse(course);
        course.getLessons().add(lesson);
        courseRepository.save(course);
        return lesson;
    }

    // توليد OTP خاص بدروس الحضور
    public String generateOtpForLesson(Long lessonId) {
        // جلب الدرس
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // توليد OTP عشوائي من 6 أرقام
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // تحديث قيم OTP في الدرس
        lesson.setOtp(otp);
        lesson.setOtpGeneratedAt(LocalDateTime.now());

        // حفظ التحديثات في قاعدة البيانات
        lessonRepository.save(lesson);

        return otp; // إرجاع الـ OTP
    }

    // التحقق من صحة الـ OTP
    public boolean verifyOtp(Long lessonId, String otp) {
        // جلب الدرس
        Optional<Lesson> optionalLesson = lessonRepository.findById(lessonId);
        if (optionalLesson.isEmpty()) {
            throw new RuntimeException("Lesson not found");
        }

        Lesson lesson = optionalLesson.get();
        // التحقق من مطابقة الـ OTP المُدخل مع المحفوظ
        return lesson.getOtp() != null && lesson.getOtp().equals(otp);
    }
}
