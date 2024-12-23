package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;
import com.learning_managment_system.model.NotificationType;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CourseService(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Course createCourse(String title, String description, String duration, String instructorName,
            MultipartFile mediaFile) {

        if (courseRepository.findByTitle(title).isPresent()) {
            throw new RuntimeException("Course already exists");
        }

        Optional<User> instructor = userRepository.findByUsername(instructorName);
        if (!instructor.isPresent()) {
            throw new RuntimeException("Instructor not found");
        }
        String mediaFileUrl = null;
        if (mediaFile != null && !mediaFile.isEmpty()) {
            mediaFileUrl = uploadMediaFile(mediaFile);
        }

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setDuration(duration);
        course.setMediaFileUrl(mediaFileUrl);
        course.setInstructor(instructor.get());
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

    public Course addLessonToCourse(String courseTitle, Lesson lesson) {
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<Lesson> foundLesson = lessonRepository.findAllByTitle(lesson.getTitle());

        if(foundLesson.stream().anyMatch(l -> course.getLessons().contains(l))){
            throw new RuntimeException("Lesson already exist in this course");
        }
        lesson.setCourse(course);
        course.getLessons().add(lesson);
        return courseRepository.save(course);
    }

    public Set<Lesson> getLessonsByCourse(String courseTitle){
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if(course.getLessons() == null || course.getLessons().isEmpty()){
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No lessons in course");
        }
        return course.getLessons();
    }


    @Transactional
    public Map<String, Object> updateCourse(String courseTitle, Course updatedCourse) {
        // Find the existing course by title
        Course existingCourse = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    
        // Track changes for notification message
        StringBuilder updateMessage = new StringBuilder("The course '" + existingCourse.getTitle() + "' has been updated: ");
    
        // Check if the title is being updated and ensure uniqueness
        if (updatedCourse.getTitle() != null && !updatedCourse.getTitle().equals(existingCourse.getTitle())) {
            courseRepository.findByTitle(updatedCourse.getTitle())
                    .ifPresent(existingCourseWithTitle -> {
                        throw new RuntimeException("Course with this title already exists");
                    });
            existingCourse.setTitle(updatedCourse.getTitle());
            updateMessage.append("Title updated to '" + updatedCourse.getTitle() + "'. ");
        }
    
        // Update the description if provided
        if (updatedCourse.getDescription() != null && !updatedCourse.getDescription().equals(existingCourse.getDescription())) {
            existingCourse.setDescription(updatedCourse.getDescription());
            updateMessage.append("Description updated to  " + updatedCourse.getDescription() +"'. ");
        }
    
        // Update the duration if provided
        if (updatedCourse.getDuration() != null && !updatedCourse.getDuration().equals(existingCourse.getDuration())) {
            existingCourse.setDuration(updatedCourse.getDuration());
            updateMessage.append("Duration updated to " + updatedCourse.getDuration() + ". ");
        }
    
        // Update the media file URL if provided
        if (updatedCourse.getMediaFileUrl() != null && !updatedCourse.getMediaFileUrl().equals(existingCourse.getMediaFileUrl())) {
            existingCourse.setMediaFileUrl(updatedCourse.getMediaFileUrl());
            updateMessage.append("Media file updated. ");
        }
    
        // Update the instructor if provided
        if (updatedCourse.getInstructor() != null && !updatedCourse.getInstructor().equals(existingCourse.getInstructor()) ) {
            userRepository.findByUsername(updatedCourse.getInstructor().getUsername())
                    .orElseThrow(() -> new RuntimeException("New Instructor not found"));
            existingCourse.setInstructor(updatedCourse.getInstructor());
            updateMessage.append("Instructor updated to  " + updatedCourse.getInstructor().getId() + "'. ");
        }
    
        // Save the updated course
        Course savedCourse = courseRepository.save(existingCourse);
    
        // Notify enrolled students about the course update
        notifyStudentsAboutCourseUpdate(savedCourse, updateMessage.toString());
    
        // Prepare the response map
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("id", savedCourse.getId());
        courseData.put("title", savedCourse.getTitle());
        courseData.put("description", savedCourse.getDescription());
        courseData.put("duration", savedCourse.getDuration());
        courseData.put("mediaFileUrl", savedCourse.getMediaFileUrl());
        return courseData;
    }
    
    // Notify enrolled students about the course update
    private void notifyStudentsAboutCourseUpdate(Course course, String updateMessage) {
        for (User student : course.getEnrolledStudents()) {
            notificationService.createCourseUpdateNotification(student.getId(), updateMessage);
        }
    }
    

    public void deleteCourse(String courseTitle) {
        if (!courseRepository.findByTitle(courseTitle).isPresent()) {
            throw new RuntimeException("Course not found");
        }
        Course course = courseRepository.findByTitle(courseTitle).get();
        if (course.getEnrolledStudents() != null) {
            throw new RuntimeException("Cannot delete course with enrolled students");
        }
        courseRepository.deleteById(course.getId());
    }

    @Transactional
    public List<Map<String, Object>> getAvailableCoursesForUser(String studentName) {
        User student = userRepository.findByUsername(studentName)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        List<Course> enrolledCourses = student.getEnrolledCourses();
        List<Course> availableCourses = courseRepository.findAll().stream()
                .filter(course -> enrolledCourses.contains(course))
                .collect(Collectors.toList());
        return availableCourses.stream().map(course -> {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", course.getId());
            courseData.put("title", course.getTitle());
            courseData.put("Instructor", course.getInstructor().getUsername());
            courseData.put("duration", course.getDuration());
            courseData.put("description", course.getDescription());
            return courseData;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<Map<String, Object>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            return new ArrayList<Map<String, Object>>();
        }
        return courses.stream().map(course -> {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", course.getId());
            courseData.put("title", course.getTitle());
            courseData.put("Instructor", course.getInstructor().getUsername());
            courseData.put("duration", course.getDuration());
            courseData.put("description", course.getDescription());
            return courseData;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Course enrollInCourse(String courseTitle, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!user.getEnrolledCourses().contains(course)) {
            user.getEnrolledCourses().add(course);
            course.getEnrolledStudents().add(user);
            userRepository.save(user);

        // Notify the instructor
        Long instructorId = course.getInstructor().getId();
        String notificationMessage = "Student " + user.getUsername() + " has enrolled in your course: " + courseTitle;
        notificationService.createNotification(instructorId, notificationMessage, NotificationType.ENROLLMENT_CONFIRMATION.name());
            // Add enrollment notification
            notificationService.createEnrollmentNotification(user.getId(), courseTitle);
        } else {
            throw new RuntimeException("You are already enrolled in this course");
        }

        return courseRepository.save(course);
    }

    @Transactional
    public List<String> getEnrolledStudentNames(String courseTitle) {
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return course.getEnrolledStudents()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

}
