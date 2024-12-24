package com.learning_managment_system.service;

import com.learning_managment_system.model.Notification;
import com.learning_managment_system.model.NotificationType;
import com.learning_managment_system.model.User;
import com.learning_managment_system.repository.NotificationRepository;
import com.learning_managment_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public Notification createNotification(Long userId, String content, String type) {
        if (userId == null || userId <= 0 || content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Invalid input for creating a notification");
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType(NotificationType.valueOf(type));
        return notificationRepository.save(notification);
    }

    // public Notification createEnrollmentNotification(Long userId, String courseTitle) {
    //     return createNotification(userId, "You have been enrolled in the course: " + courseTitle, NotificationType.ENROLLMENT_CONFIRMATION.name());
    // }
    public Notification createEnrollmentNotification(Long userId, String courseTitle) {
        return createNotification(userId, 
            "Successfully enrolled in the course: " + courseTitle, 
            NotificationType.ENROLLMENT_CONFIRMATION.name());
    }

    public Notification createGradedAssignmentNotification(Long userId, String assignmentTitle, int grade) {
        return createNotification(userId, "Your assignment '" + assignmentTitle + "' has been graded: " + grade + " points", NotificationType.GRADED_ASSIGNMENT.name());
    }

    // public Notification createCourseUpdateNotification(Long userId, String courseTitle, String updateMessage) {
    //     return createNotification(userId, "Update for course '" + courseTitle + "': " + updateMessage, NotificationType.COURSE_UPDATE.name());
    // }
    public void createCourseUpdateNotification(Long userId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(message);
        notification.setType(NotificationType.COURSE_UPDATE); // Ensure this type exists
        //notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void notifyUsers(List<String> usernames, String message) {
        if (usernames == null || usernames.isEmpty() || message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Invalid input for sending notifications");
        }
    
        for (String username : usernames) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
            createNotification(user.getId(), message, NotificationType.COURSE_UPDATE.name());
        }
    }
    
}
