package org.softuni.notificationsservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softuni.notificationsservice.model.Notification;
import org.softuni.notificationsservice.model.NotificationPreference;
import org.softuni.notificationsservice.model.NotificationStatus;
import org.softuni.notificationsservice.repository.NotificationPreferenceRepository;
import org.softuni.notificationsservice.repository.NotificationRepository;
import org.softuni.notificationsservice.web.dto.NotificationRequest;
import org.softuni.notificationsservice.web.dto.UpsertNotificationPreference;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {
    @Mock
    private  NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock
    private  NotificationRepository notificationRepository;
    @Mock
    private  MailSender mailSender;
    @Spy
    @InjectMocks
    private NotificationService notificationService;


        @Test
        void givenExistingPreference_whenUpsert_thenUpdatesAndSaves() {
            UUID userId = UUID.randomUUID();
            UpsertNotificationPreference dto = new UpsertNotificationPreference();
            dto.setUserId(userId);
            dto.setNotificationEnabled(true);
            dto.setContactInfo("test@example.com");

            NotificationPreference existingPref = NotificationPreference.builder()
                    .userId(userId)
                    .notificationEnabled(false)
                    .contactInfo("old@example.com")
                    .updatedOn(LocalDateTime.now().minusDays(1))
                    .build();

            when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existingPref));
            when(notificationPreferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

            NotificationPreference result = notificationService.upsertPreference(dto);

            assertEquals(dto.isNotificationEnabled(), result.isNotificationEnabled());
            assertEquals(dto.getContactInfo(), result.getContactInfo());
            verify(notificationPreferenceRepository, times(1)).save(existingPref);
        }

        @Test
        void givenNoExistingPreference_whenUpsert_thenCreatesNew() {
            UUID userId = UUID.randomUUID();
            UpsertNotificationPreference dto = new UpsertNotificationPreference();
            dto.setUserId(userId);
            dto.setNotificationEnabled(false);
            dto.setContactInfo("new@example.com");

            when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(notificationPreferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

            NotificationPreference result = notificationService.upsertPreference(dto);

            assertEquals(dto.getUserId(), result.getUserId());
            assertEquals(dto.getContactInfo(), result.getContactInfo());
            assertEquals(dto.isNotificationEnabled(), result.isNotificationEnabled());
            verify(notificationPreferenceRepository, times(1)).save(any(NotificationPreference.class));
        }

        @Test
    void givenUserIdForNonExistentPreference_whenGetPreferenceById_thenThrowsException(){
            UUID userId = UUID.randomUUID();
            when(notificationPreferenceRepository.findByUserId(any())).thenReturn(Optional.empty());
            assertThrows(NullPointerException.class, () -> notificationService.getPreferenceByUserId(userId));
        }

        @Test
    void givenUserIdForExistingPreference_whenGetPreferenceById_thenReturnPreference(){
            UUID userId = UUID.randomUUID();
            NotificationPreference dto = NotificationPreference.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .notificationEnabled(false)
                    .createdOn(LocalDateTime.now())
                    .contactInfo("test@email.com")
                    .updatedOn(LocalDateTime.now())
                    .build();
            when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(dto));
            NotificationPreference preference = notificationService.getPreferenceByUserId(userId);
            assertEquals(dto.getId(), preference.getId());
            assertEquals(dto.getUserId(), preference.getUserId());
            assertEquals(dto.getCreatedOn(), preference.getCreatedOn());
            assertEquals(dto.getUpdatedOn(), preference.getUpdatedOn());
        }

    @Test
    void givenEnabledPreference_whenSendNotification_thenStatusIsSucceeded() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setSubject("Subject");
        request.setBody("Body");

        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(true)
                .contactInfo("test@example.com")
                .build();

        // Spy-stub internal method
        doReturn(preference).when(notificationService).getPreferenceByUserId(userId);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Notification result = notificationService.sendNotification(request);

        assertNotNull(result); // 💥 This is now safe
        assertEquals(NotificationStatus.SUCCEEDED, result.getStatus());
        assertEquals(userId, result.getUserId());
        assertEquals("Subject", result.getSubject());
        assertEquals("Body", result.getBody());

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }




    @Test
    void givenDisabledPreference_whenSendNotification_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setSubject("subject");
        request.setBody("body");
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(false)
                .contactInfo("test@example.com")
                .build();

        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                notificationService.sendNotification(request));

        assertTrue(thrown.getMessage().contains("does not allow to receive notifications"));
        verify(mailSender, never()).send((SimpleMailMessage) any());
    }

    @Test
    void givenMailSenderFails_whenSendNotification_thenStatusIsFailed() {
        UUID userId = UUID.randomUUID();
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setSubject("subject");
        request.setBody("body");

        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(true)
                .contactInfo("test@example.com")
                .build();

        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        doThrow(new RuntimeException()).when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.sendNotification(request);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void givenHappyPath_whenGetNotificationHistory(){
        UUID userId = UUID.randomUUID();
        List<Notification> notificationsHistory = List.of(new Notification(), new Notification());
        when(notificationRepository.findAllByUserIdAndDeletedIsFalse(userId)).thenReturn(notificationsHistory);
        notificationService.getNotificationHistory(userId);
        assertEquals(2, notificationsHistory.size());

    }
    @Test
    void givenUserIdAndPreference_whenChangeNotificationPreference_thenChangeDetailsAndSaveInDatabase(){
        UUID userId = UUID.randomUUID();
        boolean enabled = true;
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(false)
                .contactInfo("test")
                .build();

        doReturn(preference).when(notificationService).getPreferenceByUserId(userId);
        notificationService.changeNotificationPreference(userId, enabled);
        assertTrue(preference.isNotificationEnabled());
        assertEquals(userId, preference.getUserId());
        verify(notificationPreferenceRepository, times(1)).save(any());
    }
    }


