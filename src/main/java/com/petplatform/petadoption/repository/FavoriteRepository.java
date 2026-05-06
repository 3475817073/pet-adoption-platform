package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    List<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId);
    Page<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
