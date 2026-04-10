package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_applications")
@Data
public class AdoptionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pet pet;

    @ManyToOne
    private User adopter;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private String reason;
    private String familySituation;
    private String contact;
    private LocalDateTime applyTime = LocalDateTime.now();
    private LocalDateTime reviewTime;
    //新增字段
    private String residenceType; // 居住类型（自有/租房/宿舍）
    private Integer housingArea;  // 住房面积（㎡）
    private String petExperience; // 养宠经验（无/有过/丰富）
    private Boolean hasOtherPets; // 是否有其他宠物
    private String otherPetsInfo; // 其他宠物详细信息（可选）

}
