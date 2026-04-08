package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "help_posts")
@Data
public class HelpPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String title;
    private String content;
    private String category;   // 物资共享 / 医疗咨询 / 经验分享

    private LocalDateTime createTime = LocalDateTime.now();
}
