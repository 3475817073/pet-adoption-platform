package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.LikeRecord;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.LikeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRecordRepository likeRecordRepository;
    private final UserService userService;

    @Transactional
    public boolean toggleLike(String username, String targetType, Long targetId) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");

        var existing = likeRecordRepository.findByUserIdAndTargetTypeAndTargetId(
                user.getId(), targetType, targetId
        );

        if (existing.isPresent()) {
            likeRecordRepository.delete(existing.get());
            return false; // 取消点赞
        } else {
            LikeRecord like = new LikeRecord();
            like.setUser(user);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeRecordRepository.save(like);
            return true; // 添加点赞
        }
    }

    public long getLikeCount(String targetType, Long targetId) {
        return likeRecordRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    public boolean isLiked(String username, String targetType, Long targetId) {
        User user = userService.findByUsername(username);
        if (user == null) return false;
        return likeRecordRepository.findByUserIdAndTargetTypeAndTargetId(
                user.getId(), targetType, targetId
        ).isPresent();
    }
}
