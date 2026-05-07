package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Message;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私信业务逻辑层
 * 负责处理发送消息、查询消息记录等业务
 */
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 发送消息
     */
    @Transactional
    public Message sendMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userService.findByUsername(senderUsername);
        if (sender == null) throw new RuntimeException("发送者不存在");

        User receiver = userService.findByUsername(receiverUsername);
        if (receiver == null) throw new RuntimeException("接收者不存在");

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("不能给自己发送消息");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content.trim());
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        try {
            notificationService.createNotification(
                    receiver.getId(),
                    "MESSAGE",
                    "收到新私信",
                    senderUsername + " 给你发了一条消息：" + (content.length() > 50 ? content.substring(0, 50) + "..." : content),
                    savedMessage.getId(),
                    senderUsername
            );
        } catch (Exception e) {
            System.err.println("创建通知失败: " + e.getMessage());
        }

        return savedMessage;
    }

    /**
     * 获取两个用户之间的消息记录
     */
    public Page<Message> getConversation(String username1, String username2, Pageable pageable) {
        User user1 = userService.findByUsername(username1);
        if (user1 == null) throw new RuntimeException("用户不存在");

        User user2 = userService.findByUsername(username2);
        if (user2 == null) throw new RuntimeException("用户不存在");

        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateTimeDesc(
                user1, user2, user1, user2, pageable);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long messageId, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");

        // 只有接收者可以标记已读
        if (!message.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("无权限操作");
        }

        message.setRead(true);
        messageRepository.save(message);
    }

    /**
     * 标记某个用户的所有消息为已读
     */
    @Transactional
    public void markAllAsRead(String receiverUsername, String senderUsername) {
        User receiver = userService.findByUsername(receiverUsername);
        if (receiver == null) throw new RuntimeException("用户不存在");

        User sender = userService.findByUsername(senderUsername);
        if (sender == null) throw new RuntimeException("用户不存在");

        // 获取所有未读消息并标记为已读
        Page<Message> unreadMessages = messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateTimeDesc(
                sender, receiver, sender, receiver, Pageable.unpaged());

        for (Message message : unreadMessages.getContent()) {
            if (message.getReceiver().getId().equals(receiver.getId()) && !message.isRead()) {
                message.setRead(true);
                messageRepository.save(message);
            }
        }
    }

    /**
     * 统计未读消息数量
     */
    public long countUnread(String username) {
        User user = userService.findByUsername(username);
        if (user == null) return 0;
        return messageRepository.countByReceiverAndReadFalse(user);
    }

    /**
     * 获取用户收到的消息
     */
    public Page<Message> getReceivedMessages(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return messageRepository.findByReceiverOrderByCreateTimeDesc(user, pageable);
    }

    /**
     * 获取用户发送的消息
     */
    public Page<Message> getSentMessages(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return messageRepository.findBySenderOrderByCreateTimeDesc(user, pageable);
    }
}