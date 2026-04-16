package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
//用户数据访问层接口
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
}