package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 通知数据访问层
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 查询用户的通知（分页）
    Page<Notification> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    // 统计用户未读通知数量
    long countByUserIdAndReadFalse(Long userId);
    
    // 删除用户的所有通知
    void deleteByUserId(Long userId);
}
