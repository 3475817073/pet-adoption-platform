package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.UserFollow;
import com.petplatform.petadoption.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户关注业务逻辑层
 * 负责处理关注、取关、查询粉丝和关注列表等业务
 */
@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserFollowRepository userFollowRepository;
    private final UserService userService;

    /**
     * 切换关注状态（关注/取关）
     */
    @Transactional
    public boolean toggleFollow(String followerUsername, String followingUsername) {
        User follower = userService.findByUsername(followerUsername);
        if (follower == null) throw new RuntimeException("关注者不存在");

        User following = userService.findByUsername(followingUsername);
        if (following == null) throw new RuntimeException("被关注者不存在");

        // 不能关注自己
        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("不能关注自己");
        }

        var existing = userFollowRepository.findByFollowerAndFollowing(follower, following);

        if (existing.isPresent()) {
            // 已关注，执行取关
            userFollowRepository.delete(existing.get());
            return false; // 取关
        } else {
            // 未关注，执行关注
            UserFollow follow = new UserFollow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            userFollowRepository.save(follow);
            return true; // 关注
        }
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowed(String followerUsername, String followingUsername) {
        User follower = userService.findByUsername(followerUsername);
        if (follower == null) return false;

        User following = userService.findByUsername(followingUsername);
        if (following == null) return false;

        return userFollowRepository.findByFollowerAndFollowing(follower, following).isPresent();
    }

    /**
     * 获取用户的粉丝列表
     */
    public Page<UserFollow> getFollowers(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return userFollowRepository.findByFollowing(user, pageable);
    }

    /**
     * 获取用户的关注列表
     */
    public Page<UserFollow> getFollowing(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return userFollowRepository.findByFollower(user, pageable);
    }

    /**
     * 统计粉丝数
     */
    public long countFollowers(String username) {
        User user = userService.findByUsername(username);
        if (user == null) return 0;
        return userFollowRepository.countByFollowing(user);
    }

    /**
     * 统计关注数
     */
    public long countFollowing(String username) {
        User user = userService.findByUsername(username);
        if (user == null) return 0;
        return userFollowRepository.countByFollower(user);
    }
}