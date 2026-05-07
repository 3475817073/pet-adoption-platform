package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.UserFollow;
import com.petplatform.petadoption.service.FollowService;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户关注控制器
 * 处理关注、取关、查询粉丝和关注列表等接口
 */
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FollowController {
    
    private final FollowService followService;
    private final UserService userService;
    
    /**
     * 切换关注状态（关注/取关）
     */
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFollow(@RequestBody Map<String, String> request) {
        try {
            String followerUsername = request.get("followerUsername");
            String followingUsername = request.get("followingUsername");
            
            boolean isFollowed = followService.toggleFollow(followerUsername, followingUsername);
            
            Map<String, Object> result = new HashMap<>();
            result.put("followed", isFollowed);
            result.put("followersCount", followService.countFollowers(followingUsername));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 检查是否已关注
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkFollow(
            @RequestParam String followerUsername,
            @RequestParam String followingUsername) {
        try {
            boolean isFollowed = followService.isFollowed(followerUsername, followingUsername);
            return ResponseEntity.ok(Map.of("followed", isFollowed));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFollow> followers = followService.getFollowers(username, pageable);
            
            // 组装用户信息
            var result = followers.map(follow -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", follow.getId());
                map.put("createTime", follow.getCreateTime());
                
                User follower = follow.getFollower();
                map.put("user", Map.of(
                        "id", follower.getId(),
                        "username", follower.getUsername(),
                        "role", follower.getRole()
                ));
                
                return map;
            });
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取用户的关注列表
     */
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFollow> following = followService.getFollowing(username, pageable);
            
            // 组装用户信息
            var result = following.map(follow -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", follow.getId());
                map.put("createTime", follow.getCreateTime());
                
                User followedUser = follow.getFollowing();
                map.put("user", Map.of(
                        "id", followedUser.getId(),
                        "username", followedUser.getUsername(),
                        "role", followedUser.getRole()
                ));
                
                return map;
            });
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 统计粉丝数
     */
    @GetMapping("/followers/count")
    public ResponseEntity<?> countFollowers(@RequestParam String username) {
        try {
            long count = followService.countFollowers(username);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 统计关注数
     */
    @GetMapping("/following/count")
    public ResponseEntity<?> countFollowing(@RequestParam String username) {
        try {
            long count = followService.countFollowing(username);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
