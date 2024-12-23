// package com.learning_managment_system.controller;

// import com.learning_managment_system.model.Notification;
// import com.learning_managment_system.service.NotificationService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/notifications")
// public class NotificationController {

//     @Autowired
//     private NotificationService notificationService;

//     public NotificationController(NotificationService notificationService) {
//         this.notificationService = notificationService;
//     }

//     @GetMapping("/readonly")
//     public ResponseEntity<?> getNotifications(
//             @RequestParam Long userId, 
//             @RequestParam(required = false, defaultValue = "true") Boolean onlyUnread) {
//         try {
//             // Fetch notifications based on the onlyUnread parameter
//             List<Notification> notifications = onlyUnread
//                     ? notificationService.getUnreadNotifications(userId)
//                     : notificationService.getAllNotifications(userId);
    
//             return ResponseEntity.ok(notifications);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of(
//                     "error", "Unable to fetch notifications",
//                     "details", e.getMessage()
//             ));
//         }
//     }
    

//     @PostMapping("/createNotifications")
//     public ResponseEntity<Notification> createNotification(
//             @RequestParam Long userId,
//             @RequestParam String content,
//             @RequestParam String type) {
//         Notification notification = notificationService.createNotification(userId, content, type);
//         return ResponseEntity.ok(notification);
//     }

//     @PatchMapping("/markAsRead")
//     public ResponseEntity<?> markAsRead(@RequestParam Long notificationId) {
//         try {
//             notificationService.markAsRead(notificationId);
//             return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of("error", "Unable to mark notification as read", "details", e.getMessage()));
//         }
//     }
// }

package com.learning_managment_system.controller;

import com.learning_managment_system.model.Notification;
import com.learning_managment_system.model.User;
import com.learning_managment_system.service.NotificationService;
import com.learning_managment_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository; // Inject UserRepository

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/show")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "true") Boolean onlyUnread
    ) {
        try {
            // Fetch user details using the authenticated username
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Retrieve notifications for the user
            Long userId = user.getId();
            List<Notification> notifications = onlyUnread
                    ? notificationService.getUnreadNotifications(userId)
                    : notificationService.getAllNotifications(userId);

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unable to fetch notifications",
                    "details", e.getMessage()
            ));
        }
    }
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // Example: Only admin can manually create notifications
    public ResponseEntity<Notification> createNotification(
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam String type) {
        Notification notification = notificationService.createNotification(userId, content, type);
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/markAsRead")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> markAsRead(@RequestParam Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unable to mark notification as read", "details", e.getMessage()));
        }
    }
}
