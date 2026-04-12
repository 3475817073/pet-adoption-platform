package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


// 评论
@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的互助帖子 */
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private HelpPost post;

    /** 发表评论的用户 */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 评论内容，最大长度 500 字符 */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 父级评论（用于实现回复功能）
     * 若为 null 则表示该评论为主评论，否则表示是对某条评论的回复
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /** 评论创建时间 */
    private LocalDateTime createTime = LocalDateTime.now();
}
