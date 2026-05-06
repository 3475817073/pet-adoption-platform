package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


// 帖子
@Entity
@Table(name = "help_posts")
@Data
public class HelpPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;//用户

    private String title;//标题

    @Column(columnDefinition = "TEXT")
    private String content;//内容

    private String category;   //分类

    private String city;  // 所在城市

    @Column(columnDefinition = "TEXT")
    private String photoUrls;  // 图片URL列表

    //审核状态
    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.PENDING;

    // 拒绝理由
    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    private LocalDateTime createTime = LocalDateTime.now();//创建时间

    @ManyToOne
    @JoinColumn(name = "related_pet_id")
    private Pet relatedPet; // 可选：关联的宠物
}
