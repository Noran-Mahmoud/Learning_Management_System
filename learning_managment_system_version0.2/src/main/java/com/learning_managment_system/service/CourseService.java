package com.learning_managment_system.service;

import com.learning_managment_system.model.Course;
import com.learning_managment_system.model.Lesson;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.CourseRepository;
import com.learning_managment_system.repository.LessonRepository;
import com.learning_managment_system.repository.UserRepository;
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

    public CourseService(CourseRepository courseRepository, LessonRepository lessonRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    public Course createCourse(String title, String description
            , String duration, String instructorName, MultipartFile mediaFile) {

        if(courseRepository.findByTitle(title).isPresent()) {
            throw new RuntimeException("Course already exists");
        }

        Optional <User> instructor = userRepository.findByUsername(instructorName);
        if(!instructor.isPresent()) {
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


    public Map<String, Object> updateCourse(String courseTitle, Course updatedCourse) {
        Optional<Course> existingCourseOpt = courseRepository.findByTitle(courseTitle);

        if (!existingCourseOpt.isPresent()) {
            throw new RuntimeException("Course not found");
        }
        Course existingCourse = existingCourseOpt.get();

        if (updatedCourse.getTitle() != null) {
            courseRepository.findByTitle(updatedCourse.getTitle()).ifPresent(_-> {
                    throw new RuntimeException("Course with this title already exists");});
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
        if (updatedCourse.getInstructor() != null) {
            userRepository.findByUsername(updatedCourse.getInstructor().getUsername())
                    .orElseThrow(() -> new RuntimeException("New Instructor not found"));
            existingCourse.setInstructor(updatedCourse.getInstructor());
        }


        Course savedCourse = courseRepository.save(existingCourse);

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("id", savedCourse.getId());
        courseData.put("title", savedCourse.getTitle());
        courseData.put("description", savedCourse.getDescription());
        courseData.put("duration", savedCourse.getDuration());
        courseData.put("mediaFileUrl", savedCourse.getMediaFileUrl());
        return courseData;
    }

    public void deleteCourse(String courseTitle) {
        if (!courseRepository.findByTitle(courseTitle).isPresent()) {
            throw new RuntimeException("Course not found");
        }
        Course course = courseRepository.findByTitle(courseTitle).get();
        if(course.getEnrolledStudents() != null){
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
        if(courses.isEmpty()){
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
