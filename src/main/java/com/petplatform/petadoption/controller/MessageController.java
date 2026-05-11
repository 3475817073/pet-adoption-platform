package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.Message;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.service.MessageService;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户私信控制器
 * 处理发送消息、查询消息记录、标记已读等接口
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String senderUsername = request.get("senderUsername");
            String receiverUsername = request.get("receiverUsername");
            String content = request.get("content");
            String imageUrl = request.get("imageUrl");

            Message message = messageService.sendMessage(senderUsername, receiverUsername, content, imageUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("id", message.getId());
            result.put("content", message.getContent());
            result.put("imageUrl", message.getImageUrl());
            result.put("createTime", message.getCreateTime());
            result.put("read", message.isRead());
            result.put("sender", Map.of(
                    "id", message.getSender().getId(),
                    "username", message.getSender().getUsername()
            ));
            result.put("receiver", Map.of(
                    "id", message.getReceiver().getId(),
                    "username", message.getReceiver().getUsername()
            ));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取两个用户之间的对话记录
     */
    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(
            @RequestParam String username1,
            @RequestParam String username2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageService.getConversation(username1, username2, pageable);

            // 组装消息信息
            var result = messages.map(message -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", message.getId());
                map.put("content", message.getContent());
                map.put("imageUrl", message.getImageUrl());
                map.put("read", message.isRead());
                map.put("createTime", message.getCreateTime());

                map.put("sender", Map.of(
                        "id", message.getSender().getId(),
                        "username", message.getSender().getUsername()
                ));
                map.put("receiver", Map.of(
                        "id", message.getReceiver().getId(),
                        "username", message.getReceiver().getUsername()
                ));

                return map;
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{messageId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long messageId,
            @RequestParam String username) {
        try {
            messageService.markAsRead(messageId, username);
            return ResponseEntity.ok("已标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestParam String receiverUsername,
            @RequestParam String senderUsername) {
        try {
            messageService.markAllAsRead(receiverUsername, senderUsername);
            return ResponseEntity.ok("所有消息已标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 统计未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnread(@RequestParam String username) {
        try {
            long count = messageService.countUnread(username);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
