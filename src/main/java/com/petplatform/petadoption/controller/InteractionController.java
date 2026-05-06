package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.FavoriteService;
import com.petplatform.petadoption.service.HelpPostService;
import com.petplatform.petadoption.service.LikeService;
import com.petplatform.petadoption.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InteractionController {

    private final FavoriteService favoriteService;
    private final LikeService likeService;
    private final PetService petService;
    private final HelpPostService helpPostService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/favorite/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String targetType = (String) request.get("targetType");
            Long targetId = Long.valueOf(request.get("targetId").toString());

            boolean favorited = favoriteService.toggleFavorite(username, targetType, targetId);
            return ResponseEntity.ok(Map.of("favorited", favorited));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取用户收藏列表(分页)
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getUserFavorites(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            Page<Favorite> favorites = favoriteService.getUserFavorites(username, pageable);

            // 组装详细信息
            var result = favorites.map(fav -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", fav.getId());
                map.put("targetType", fav.getTargetType());
                map.put("targetId", fav.getTargetId());
                map.put("createTime", fav.getCreateTime());

                if ("PET".equals(fav.getTargetType())) {
                    Pet pet = petService.findById(fav.getTargetId());
                    map.put("detail", pet);
                } else if ("POST".equals(fav.getTargetType())) {
                    HelpPost post = helpPostService.findById(fav.getTargetId());
                    map.put("detail", post);
                }
                return map;
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/favorite/check")
    public ResponseEntity<?> checkFavorite(
            @RequestParam String username,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            boolean favorited = favoriteService.isFavorited(username, targetType, targetId);
            return ResponseEntity.ok(Map.of("favorited", favorited));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 切换点赞状态
     */
    @PostMapping("/like/toggle")
    public ResponseEntity<?> toggleLike(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String targetType = (String) request.get("targetType");
            Long targetId = Long.valueOf(request.get("targetId").toString());

            boolean liked = likeService.toggleLike(username, targetType, targetId);
            long count = likeService.getLikeCount(targetType, targetId);

            return ResponseEntity.ok(Map.of("liked", liked, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取点赞数
     */
    @GetMapping("/like/count")
    public ResponseEntity<?> getLikeCount(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            long count = likeService.getLikeCount(targetType, targetId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/like/check")
    public ResponseEntity<?> checkLike(
            @RequestParam String username,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        try {
            boolean liked = likeService.isLiked(username, targetType, targetId);
            return ResponseEntity.ok(Map.of("liked", liked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
