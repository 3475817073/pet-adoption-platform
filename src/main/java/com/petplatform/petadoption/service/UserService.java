package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务逻辑层
 * 负责处理用户注册、登录校验及信息查询等核心业务
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikeRecordRepository likeRecordRepository;
    private final UserFollowRepository userFollowRepository;
    private final HelpPostRepository helpPostRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final VisitRecordRepository visitRecordRepository;

    /**
     * 根据用户名查找用户信息
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 检查用户名是否已被注册
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 保存或更新用户信息（用于注册或资料修改）
     */
    public User save(User user) {
        return userRepository.save(user);
    }


    /**
     * 分页获取用户列表
     */
    public org.springframework.data.domain.Page<User> getUsers(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户（根据用户名或真实姓名模糊匹配）
     */
    public org.springframework.data.domain.Page<User> searchUsers(String keyword, org.springframework.data.domain.Pageable pageable) {
        return userRepository.findByUsernameContainingOrRealNameContaining(keyword, keyword, pageable);
    }


    /**
     * 删除用户及其所有相关数据（级联删除）
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 1. 删除用户的通知
        notificationRepository.deleteByUserId(userId);

        // 2. 删除用户的私信（作为发送者和接收者）
        User user = findById(userId);
        if (user != null) {
            messageRepository.findBySenderOrderByCreateTimeDesc(user, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .forEach(message -> messageRepository.deleteById(message.getId()));
            messageRepository.findByReceiverOrderByCreateTimeDesc(user, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .forEach(message -> messageRepository.deleteById(message.getId()));
        }

        // 3. 删除用户的评论
        commentRepository.findAll().stream()
                .filter(comment -> comment.getUser().getId().equals(userId))
                .forEach(comment -> commentRepository.deleteById(comment.getId()));

        // 4. 删除用户发布的互助帖
        helpPostRepository.findAll().stream()
                .filter(post -> post.getUser().getId().equals(userId))
                .forEach(post -> helpPostRepository.deleteById(post.getId()));

        // 5. 删除用户的关注关系（关注者和被关注者）
        userFollowRepository.findAll().stream()
                .filter(follow -> follow.getFollower().getId().equals(userId) || follow.getFollowing().getId().equals(userId))
                .forEach(follow -> userFollowRepository.deleteById(follow.getId()));

        // 6. 删除用户的点赞记录
        likeRecordRepository.findAll().stream()
                .filter(record -> record.getUser().getId().equals(userId))
                .forEach(record -> likeRecordRepository.deleteById(record.getId()));

        // 7. 删除用户的收藏
        favoriteRepository.findAll().stream()
                .filter(favorite -> favorite.getUser().getId().equals(userId))
                .forEach(favorite -> favoriteRepository.deleteById(favorite.getId()));

        // 8. 删除用户的回访记录
        visitRecordRepository.findAll().stream()
                .filter(record -> record.getVisitor().getId().equals(userId))
                .forEach(record -> visitRecordRepository.deleteById(record.getId()));

        // 9. 删除用户的领养申请
        adoptionApplicationRepository.findAll().stream()
                .filter(application -> application.getAdopter().getId().equals(userId))
                .forEach(application -> adoptionApplicationRepository.deleteById(application.getId()));

        // 10. 删除用户发布的宠物
        petRepository.findAll().stream()
                .filter(pet -> pet.getRescuer().getId().equals(userId))
                .forEach(pet -> petRepository.deleteById(pet.getId()));

        // 11. 最后删除用户本身
        userRepository.deleteById(userId);
    }
}

