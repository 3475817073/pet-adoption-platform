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

    @Column(length = 1000)
    private String photoUrls;//图片地址

    @Enumerated(EnumType.STRING)
    private PetStatus status = PetStatus.AVAILABLE;//默认状态

    @ManyToOne
    @JoinColumn(name = "rescuer_id")
    private User rescuer;//救援者

    private LocalDateTime createTime = LocalDateTime.now();
}
