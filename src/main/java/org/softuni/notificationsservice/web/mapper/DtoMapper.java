package org.softuni.notificationsservice.web.mapper;

import lombok.experimental.UtilityClass;
import org.softuni.notificationsservice.model.Notification;
import org.softuni.notificationsservice.model.NotificationPreference;
import org.softuni.notificationsservice.web.dto.NotificationPreferenceResponse;
import org.softuni.notificationsservice.web.dto.NotificationResponse;

@UtilityClass
public class DtoMapper {
    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference entity){
        return NotificationPreferenceResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .enabled(entity.isNotificationEnabled())
                .contactInfo(entity.getContactInfo())
                .build();
    }

    public static NotificationResponse fromNotification(Notification notification){
        return NotificationResponse.builder()
                .status(notification.getStatus())
                .subject(notification.getSubject())
                .createdOn(notification.getCreatedOn())
                .build();
    }
}
