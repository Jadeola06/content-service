package com.flexydemy.content.service;


import com.flexydemy.content.dto.NotificationResponse;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.enums.NotificationTypes;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.Notification;
import com.flexydemy.content.repository.NotificationRepository;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userRepository;

    public Notification sendNotification(String userId, NotificationTypes type, String title, String content, String relatedEntityId, String senderId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedEntityId(relatedEntityId)
                .senderId(senderId)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    public List<NotificationResponse> getUserNotifications(String userId, HttpServletRequest request) {
        validateUserId(userId, request);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(x -> {
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setId(x.getId());
            notificationResponse.setContent(x.getContent());
            notificationResponse.setUserId(x.getUserId());
            notificationResponse.setTime(x.getCreatedAt());
            notificationResponse.setType(x.getType());

            return notificationResponse;

        }).collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(String userId) {
        return notificationRepository.countByUserIdAndReadIsFalse(userId);
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private void validateUserId(String userId, HttpServletRequest request) {
        String token = Utils.getToken(request);
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("Tutor ID must not be null or empty.");
        }

        userRepository.findById(userId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", userId)))
                .block();
    }
}

