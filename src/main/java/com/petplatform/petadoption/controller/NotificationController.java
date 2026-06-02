package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.Notification;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.service.NotificationService;
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
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    /**
     * 获取用户的通知列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getNotifications(
            @RequestParam String username, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {User user = userService.findByUsername(username);
            if (user == null) {return ResponseEntity.badRequest().body("用户不存在");}
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationService.getUserNotifications(user.getId(), pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {return ResponseEntity.badRequest().body(e.getMessage());}}
    
    /**
     * 统计未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnread(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            long count = notificationService.countUnread(user.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 标记通知为已读
     */
    @PostMapping("/read/{notificationId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            notificationService.markAsRead(notificationId, user.getId());
            return ResponseEntity.ok("已标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok("所有通知已标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            notificationService.deleteNotification(notificationId, user.getId());
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
