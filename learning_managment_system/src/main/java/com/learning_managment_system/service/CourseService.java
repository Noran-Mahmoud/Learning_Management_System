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

    
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    
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

    
    public Lesson addLessonToCourse(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        lesson.setCourse(course);
        course.getLessons().add(lesson);
        courseRepository.save(course);
        return lesson;
    }

    
    public String generateOtpForLesson(Long lessonId) {
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        
        lesson.setOtp(otp);
        lesson.setOtpGeneratedAt(LocalDateTime.now());

        
        lessonRepository.save(lesson);

        return otp; 
    }

    
    public boolean verifyOtp(Long lessonId, String otp) {
        
        Optional<Lesson> optionalLesson = lessonRepository.findById(lessonId);
        if (optionalLesson.isEmpty()) {
            throw new RuntimeException("Lesson not found");
        }

        Lesson lesson = optionalLesson.get();
        
        return lesson.getOtp() != null && lesson.getOtp().equals(otp);
    }
}
