package org.softuni.notificationsservice.web.dto;

import lombok.Builder;
import lombok.Data;
import org.softuni.notificationsservice.model.NotificationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String subject;

    private LocalDateTime createdOn;

    private NotificationStatus status;

}
