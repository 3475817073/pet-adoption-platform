package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Notification;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知业务逻辑层
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    /**
     * 创建通知
     */
    @Transactional
    public Notification createNotification(Long userId, String type, String title, String content, Long relatedId, String senderUsername) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setSenderUsername(senderUsername);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    /**
     * 获取用户的通知列表（分页）
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    /**
     * 统计用户未读通知数量
     */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, Pageable.unpaged());
        for (Notification notification : notifications.getContent()) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        notificationRepository.delete(notification);
    }
}
