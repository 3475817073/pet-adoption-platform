package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户私信实体类
 * 记录用户之间的私信消息
 */
@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发送者
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // 接收者
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 是否已读
    @Column(name = "`read`")
    private boolean read = false;

    private LocalDateTime createTime = LocalDateTime.now();
}