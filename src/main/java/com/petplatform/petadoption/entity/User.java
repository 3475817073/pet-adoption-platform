package com.petplatform.petadoption.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String realName;     // 真实姓名

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;           // 角色

    private LocalDateTime createTime = LocalDateTime.now();
}

