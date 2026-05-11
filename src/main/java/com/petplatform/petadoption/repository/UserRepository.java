package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Role;
import com.petplatform.petadoption.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    Page<User> findAll(Pageable pageable);
    /**
     * 根据关键词搜索用户（用户名或真实姓名模糊匹配）
     */
    Page<User> findByUsernameContainingOrRealNameContaining(String keyword, String keyword2, Pageable pageable);

    /**
     * 根据角色查询用户
     */
    List<User> findByRole(Role role);
}