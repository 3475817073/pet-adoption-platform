package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//互助帖子数据访问层接口，提供根据分类查询的接口
public interface HelpPostRepository extends JpaRepository<HelpPost, Long> {

    /**
     * 根据分类查询互助帖子列表
     */
    List<HelpPost> findByCategory(String category);

    /**
     * 根据状态查询互助帖子列表
     */
    Page<HelpPost> findByStatus(PostStatus status, Pageable pageable);

    /**
     * 根据宠物类型查询审核通过的帖子（优先匹配关联宠物，其次匹配帖子内容关键词）
     * 如果帖子有关联宠物且类型匹配，或者帖子标题/内容包含宠物类型关键词，都会被返回
     */
    @Query("SELECT h FROM HelpPost h WHERE h.status = :status AND " +
            "(h.relatedPet.type = :petType OR " +
            "LOWER(h.title) LIKE LOWER(CONCAT('%', :petType, '%')) OR " +
            "LOWER(h.content) LIKE LOWER(CONCAT('%', :petType, '%')))")
    Page<HelpPost> findApprovedByPetType(
            @Param("petType") String petType,
            @Param("status") PostStatus status, Pageable pageable);


    /**
     * 根据宠物类型查询帖子数量
     */
    long countByRelatedPet_TypeAndStatus(String petType, PostStatus status);


    /**
     * 分页查询指定用户的帖子，按创建时间倒序
     * 用于个人中心展示用户发布的互助帖
     */
    Page<HelpPost> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);


    /**
     * 根据关联的宠物ID查询帖子列表
     */
    List<HelpPost> findByRelatedPetId(Long relatedPetId);


}