package org.softuni.notificationsservice.web;

import org.softuni.notificationsservice.model.Notification;
import org.softuni.notificationsservice.model.NotificationPreference;
import org.softuni.notificationsservice.service.NotificationService;
import org.softuni.notificationsservice.web.dto.NotificationPreferenceResponse;
import org.softuni.notificationsservice.web.dto.NotificationRequest;
import org.softuni.notificationsservice.web.dto.NotificationResponse;
import org.softuni.notificationsservice.web.dto.UpsertNotificationPreference;
import org.softuni.notificationsservice.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/preferences")
    private ResponseEntity<NotificationPreferenceResponse> notificationPreferenceResponse(@RequestBody UpsertNotificationPreference upsertNotificationPreference){
        NotificationPreference notificationPreference = notificationService.upsertPreference(upsertNotificationPreference);
        NotificationPreferenceResponse response = DtoMapper.fromNotificationPreference(notificationPreference);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/preferences")
    private ResponseEntity<NotificationPreferenceResponse> getUserNotificationPreference(@RequestParam(name = "userId") UUID userId){
        NotificationPreference preference = notificationService.getPreferenceByUserId(userId);
        NotificationPreferenceResponse response = DtoMapper.fromNotificationPreference(preference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping
    private ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request){

        Notification notification = notificationService.sendNotification(request);
        NotificationResponse notificationResponse = DtoMapper.fromNotification(notification);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationResponse);
    }

    @GetMapping
    private ResponseEntity<List<NotificationResponse>>getNotificationHistory(@RequestParam(name = "user_id")UUID userId){
        List<NotificationResponse> notificationResponses = notificationService.getNotificationHistory(userId).stream().map(DtoMapper::fromNotification).toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationResponses);
    }
}
