package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, LessonRepository lessonRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;

    }


    public Course createCourse(Course course) {
        return courseRepository.save(course);
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


    public Map<String, Object> updateCourse(Long courseId, Course updatedCourse) {
        Optional<Course> existingCourseOpt = courseRepository.findById(courseId);

        if (existingCourseOpt.isPresent()) {
            Course existingCourse = existingCourseOpt.get();

            if (updatedCourse.getTitle() != null) {
                existingCourse.setTitle(updatedCourse.getTitle());
            }
            if (updatedCourse.getDescription() != null) {
                existingCourse.setDescription(updatedCourse.getDescription());
            }
            if (updatedCourse.getDuration() != null) {
                existingCourse.setDuration(updatedCourse.getDuration());
            }
            if (updatedCourse.getMediaFileUrl() != null) {
                existingCourse.setMediaFileUrl(updatedCourse.getMediaFileUrl());
            }

            Course savedCourse = courseRepository.save(existingCourse);


            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", savedCourse.getId());
            courseData.put("title", savedCourse.getTitle());
            courseData.put("description", savedCourse.getDescription());
            courseData.put("duration", savedCourse.getDuration());
            courseData.put("mediaFileUrl", savedCourse.getMediaFileUrl());

            return courseData;
        } else {
            throw new RuntimeException("Course not found");
        }
    }


    public void deleteCourse(Long courseId) {
        if (courseRepository.existsById(courseId)) {
            courseRepository.deleteById(courseId);
        } else {
            throw new RuntimeException("Course not found");
        }
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


public List<Map<String, Object>> getAvailableCoursesForStudent(Long studentId) {

    User student = userRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));

    List<Course> enrolledCourses = student.getEnrolledCourses();


    List<Course> availableCourses = courseRepository.findAll().stream()
            .filter(course -> !enrolledCourses.contains(course))
            .collect(Collectors.toList());


    return availableCourses.stream().map(course -> {
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("id", course.getId());
        courseData.put("title", course.getTitle());
        courseData.put("description", course.getDescription());
        courseData.put("duration", course.getDuration());
        return courseData;
    }).collect(Collectors.toList());
}

    public Course enrollInCourse(Long courseId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!user.getEnrolledCourses().contains(course)) {
            user.getEnrolledCourses().add(course);
            userRepository.save(user);
        } else {
            throw new RuntimeException("You are already enrolled in this course");
        }

        return course;
    }


    public List<String> getEnrolledStudentNames(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));


        return course.getEnrolledStudents()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
}
