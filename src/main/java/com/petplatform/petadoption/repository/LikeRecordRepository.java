package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    Optional<LikeRecord> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);
}
