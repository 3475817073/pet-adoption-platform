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

    private String name;
    private String type;
    private String gender;
    private Integer age;
    private String description;

    private String photoUrl;

    @Column(length = 1000)
    private String photoUrls;

    @Enumerated(EnumType.STRING)
    private PetStatus status = PetStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "rescuer_id")
    private User rescuer;

    private LocalDateTime createTime = LocalDateTime.now();
}
