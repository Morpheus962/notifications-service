package org.softuni.notificationsservice.service;

import lombok.extern.slf4j.Slf4j;
import org.softuni.notificationsservice.model.Notification;
import org.softuni.notificationsservice.model.NotificationPreference;
import org.softuni.notificationsservice.model.NotificationStatus;
import org.softuni.notificationsservice.repository.NotificationPreferenceRepository;
import org.softuni.notificationsservice.repository.NotificationRepository;
import org.softuni.notificationsservice.web.dto.NotificationRequest;
import org.softuni.notificationsservice.web.dto.UpsertNotificationPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Autowired
    public NotificationService(NotificationPreferenceRepository notificationPreferenceRepository, NotificationRepository notificationRepository, MailSender mailSender) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference dto) {
        Optional<NotificationPreference> userNotificationPreferenceOptional = notificationPreferenceRepository.findByUserId(dto.getUserId());

        if(userNotificationPreferenceOptional.isPresent()){
            NotificationPreference notificationPreference = userNotificationPreferenceOptional.get();
            notificationPreference.setNotificationEnabled(dto.isNotificationEnabled());
            notificationPreference.setContactInfo(dto.getContactInfo());
            notificationPreference.setUpdatedOn(LocalDateTime.now());
            return  notificationPreferenceRepository.save(notificationPreference);
        }

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(dto.getUserId())
                .contactInfo(dto.getContactInfo())
                .notificationEnabled(dto.isNotificationEnabled())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return notificationPreferenceRepository.save(notificationPreference);

    }
    public NotificationPreference getPreferenceByUserId(UUID userId) {

        return notificationPreferenceRepository.findByUserId(userId).orElseThrow( () -> new NullPointerException("Notification preference for user id %s was not found".formatted(userId)));
    }

    public Notification sendNotification(NotificationRequest request) {
        UUID userId = request.getUserId();
        NotificationPreference preference = getPreferenceByUserId(request.getUserId());
        if (!preference.isNotificationEnabled()){
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(preference.getContactInfo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        Notification notification = Notification.builder()
                .subject(request.getSubject())
                .body(request.getBody())
                .createdOn(LocalDateTime.now())
                .userId(userId)
                .isDeleted(false)
                .build();
        try{
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e){
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending an email to %s due to %s".formatted(preference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        return notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);

    }
}
