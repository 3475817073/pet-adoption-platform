package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 * 记录用户之间的关注关系
 */
@Entity
@Table(name = "user_follows")
@Data
public class UserFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关注者
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // 被关注者
    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    private LocalDateTime createTime = LocalDateTime.now();
}