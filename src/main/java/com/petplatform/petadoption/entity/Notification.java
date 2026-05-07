package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 记录用户收到的各种通知（私信、评论等）
 */
@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 接收通知的用户
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 通知类型：MESSAGE(私信), COMMENT(评论)
    @Column(nullable = false, length = 20)
    private String type;

    // 通知标题
    @Column(nullable = false, length = 100)
    private String title;

    // 通知内容
    @Column(columnDefinition = "TEXT")
    private String content;

    // 关联的ID（消息ID或评论ID）
    private Long relatedId;

    // 发送者用户名
    @Column(length = 50)
    private String senderUsername;

    // 是否已读
    @Column(name = "`read`")
    private boolean read = false;

    // 创建时间
    private LocalDateTime createTime = LocalDateTime.now();
}
