package org.softuni.notificationsservice.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpsertNotificationPreference {
    @NotNull
    private UUID userId;

    private boolean notificationEnabled;

    private String contactInfo;

}
