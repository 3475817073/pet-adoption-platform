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
    private String content;//内容
    private String category;   //分类：物资共享 / 医疗咨询 / 经验分享

    private LocalDateTime createTime = LocalDateTime.now();//创建时间
}
