package com.learning_managment_system.service;

import com.learning_managment_system.model.Notification;
import com.learning_managment_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service
// public class NotificationService {

//     @Autowired
//     private NotificationRepository notificationRepository;

//     public List<Notification> getNotifications(Long userId, Boolean onlyUnread) {
//         return onlyUnread
//                 ? notificationRepository.findByUserIdAndIsRead(userId, false)
//                 : notificationRepository.findByUserId(userId);
//     }

//     public Notification createNotification(Long userId, String content, String type) {
//         Notification notification = new Notification();
//         notification.setUserId(userId);
//         notification.setContent(content);
//         notification.setType(type);
//         return notificationRepository.save(notification);
//     }

//     public void markAsRead(Long notificationId) {
//         Notification notification = notificationRepository.findById(notificationId)
//                 .orElseThrow(() -> new RuntimeException("Notification not found"));
//         notification.setIsRead(true);
//         notificationRepository.save(notification);
//     }
// }

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    // public List<Notification> getNotifications(Long userId, Boolean onlyUnread) {
    //     if (userId == null || userId <= 0) {
    //         throw new IllegalArgumentException("Invalid userId");
    //     }
    //     return onlyUnread
    //             ? notificationRepository.findByUserIdAndIsRead(userId, false)
    //             : notificationRepository.findByUserId(userId);
    // }

    public Notification createNotification(Long userId, String content, String type) {
        if (userId == null || userId <= 0 || content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Invalid input for creating a notification");
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
