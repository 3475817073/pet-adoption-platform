package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * 用户关注数据访问层接口
 */
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 查询某个用户的所有关注者
     */
    Page<UserFollow> findByFollowing(User following, Pageable pageable);

    /**
     * 查询某个用户关注的所有人
     */
    Page<UserFollow> findByFollower(User follower, Pageable pageable);

    /**
     * 查询关注关系是否存在
     */
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    /**
     * 统计某个用户的粉丝数
     */
    long countByFollowing(User following);

    /**
     * 统计某个用户的关注数
     */
    long countByFollower(User follower);

    /**
     * 获取某个用户的所有粉丝列表
     */
    List<UserFollow> findAllByFollowing(User following);

    /**
     * 获取某个用户关注的所有人列表
     */
    List<UserFollow> findAllByFollower(User follower);
}