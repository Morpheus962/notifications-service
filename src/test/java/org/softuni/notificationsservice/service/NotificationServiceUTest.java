package org.softuni.notificationsservice.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softuni.notificationsservice.repository.NotificationPreferenceRepository;
import org.softuni.notificationsservice.repository.NotificationRepository;
import org.springframework.mail.MailSender;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {
    @Mock
    private  NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock
    private  NotificationRepository notificationRepository;
    @Mock
    private  MailSender mailSender;
    @InjectMocks
    private NotificationService notificationService;
}
