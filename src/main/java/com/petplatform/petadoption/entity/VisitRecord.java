package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_records")
@Data
public class VisitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private AdoptionApplication application;  // 关联的领养申请

    @ManyToOne
    @JoinColumn(name = "visitor_id", nullable = false)
    private User visitor;  // 回访人（管理员）

    @Column(nullable = false)
    private String visitType;  // 回访方式：电话 /上门 / 在线

    @Column(nullable = false)
    private LocalDateTime visitTime;  // 回访时间

    @Column(nullable = false)
    private String petStatus;  // 宠物状态：适应良好/ 适应中 / 存在问题

    @Column(length = 2000)
    private String content;  // 回访内容

    @Column(length = 2000)
    private String feedback;  // 领养者反馈

    private Boolean needFollowUp = false;  // 是否需要后续回访
    private LocalDateTime nextVisitTime;  // 下次回访时间

    private LocalDateTime createTime = LocalDateTime.now();
}
