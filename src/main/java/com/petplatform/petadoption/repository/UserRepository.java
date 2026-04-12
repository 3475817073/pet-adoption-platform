package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
//用户数据访问层接口
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 包含用户信息的 Optional 对象，若未找到则返回空
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     * @param username 待检查的用户名
     * @return 若用户名已存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);
}