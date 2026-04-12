package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// 领养申请
@Entity
@Table(name = "adoption_applications")
@Data
public class AdoptionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pet pet;//关联的宠物信息

    @ManyToOne
    private User adopter;//发起申请的用户

    // 申请状态
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private String reason;//领养理由
    private String familySituation;//家庭情况
    private String contact;//联系方式
    private LocalDateTime applyTime = LocalDateTime.now();//申请时间
    private LocalDateTime reviewTime;//审核时间

    private String residenceType; // 居住类型（自有/租房/宿舍）
    private Integer housingArea;  // 住房面积（㎡）
    private String petExperience; // 养宠经验（无/有过/丰富）
    private Boolean hasOtherPets; // 是否有其他宠物
    private String otherPetsInfo; // 其他宠物详细信息（可选）

}
