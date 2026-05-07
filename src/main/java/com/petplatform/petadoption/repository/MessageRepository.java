package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Message;
import com.petplatform.petadoption.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 私信数据访问层接口
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询两个用户之间的所有消息（按时间排序）
     */
    Page<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByCreateTimeDesc(
            User user1, User user2, User user3, User user4, Pageable pageable);

    /**
     * 获取用户的所有对话列表（每个对话的最新一条消息）
     */
    List<Message> findTop1BySenderAndReceiverOrderByCreateTimeDesc(User sender, User receiver);

    /**
     * 统计用户未读消息数量
     */
    long countByReceiverAndReadFalse(User receiver);

    /**
     * 查询用户收到的所有消息
     */
    Page<Message> findByReceiverOrderByCreateTimeDesc(User receiver, Pageable pageable);

    /**
     * 查询用户发送的所有消息
     */
    Page<Message> findBySenderOrderByCreateTimeDesc(User sender, Pageable pageable);
}