package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;//姓名
    private String type;//种类
    private String gender;//公母
    private Integer age;//年龄
    private String description;//描述

    private String photoUrl;//图片地址
    private String tags;//宠物标签

    @Column(name = "vaccinated")
    private boolean vaccinated = false;

    @Column(name = "neutered")
    private boolean neutered = false;

    @Column(name = "dewormed")
    private boolean dewormed = false;


    @Column(length = 1000)
    private String photoUrls;//图片地址

    @Enumerated(EnumType.STRING)
    private PetStatus status = PetStatus.AVAILABLE;//默认状态

    // 审核状态
    @Enumerated(EnumType.STRING)
    private PostStatus reviewStatus = PostStatus.PENDING;//默认待审核

    @ManyToOne
    @JoinColumn(name = "rescuer_id")
    private User rescuer;//宠物发布者

    private LocalDateTime createTime = LocalDateTime.now();
}
