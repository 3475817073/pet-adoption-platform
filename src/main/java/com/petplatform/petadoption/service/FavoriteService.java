package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Favorite;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;

    @Transactional
    public boolean toggleFavorite(String username, String targetType, Long targetId) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");

        var existing = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(
                user.getId(), targetType, targetId
        );

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // 取消收藏
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setTargetType(targetType);
            favorite.setTargetId(targetId);
            favoriteRepository.save(favorite);
            return true; // 添加收藏
        }
    }

    public Page<Favorite> getUserFavorites(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return favoriteRepository.findByUserIdOrderByCreateTimeDesc(user.getId(), pageable);
    }

    public boolean isFavorited(String username, String targetType, Long targetId) {
        User user = userService.findByUsername(username);
        if (user == null) return false;
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(
                user.getId(), targetType, targetId
        ).isPresent();
    }
}
