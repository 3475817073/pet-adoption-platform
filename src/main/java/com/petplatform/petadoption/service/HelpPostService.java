package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.entity.PostStatus;
import com.petplatform.petadoption.repository.FavoriteRepository;
import com.petplatform.petadoption.repository.HelpPostRepository;
import com.petplatform.petadoption.repository.LikeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 互助帖子业务逻辑层
 * 负责处理互助帖子的发布、查询及分页检索等核心业务
 */
@Service
@RequiredArgsConstructor
public class HelpPostService {
    private final HelpPostRepository helpPostRepository;
    private final CommentService commentService;
    private final FavoriteRepository favoriteRepository;
    private final LikeRecordRepository likeRecordRepository;

    /**
     * 保存或更新互助帖子信息
     */
    public HelpPost save(HelpPost helpPost) {

        return helpPostRepository.save(helpPost);
    }

    /**
     * 查询系统中所有的互助帖子
     */
    public List<HelpPost> findAll() {

        return helpPostRepository.findAll();
    }

    /**
     * 根据 ID 查找单个互助帖子
     */
    public HelpPost findById(Long id) {

        return helpPostRepository.findById(id).orElse(null);
    }

    /**
     * 分页查询互助帖子列表
     */
    public Page<HelpPost> findPage(Pageable pageable) {

        return helpPostRepository.findAll(pageable);
    }

    /**
     * 根据 ID 删除互助帖子
     */
    @Transactional
    public void deleteById(Long id) {
        helpPostRepository.deleteById(id);
    }

    /**
     * 分页查询审核通过的帖子
     */
    public Page<HelpPost> findApprovedPage(Pageable pageable) {
        return helpPostRepository.findByStatus(PostStatus.APPROVED, pageable);
    }

    /**
     * 分页查询待审核的帖子
     */
    public Page<HelpPost> findPendingPage(Pageable pageable) {
        return helpPostRepository.findByStatus(PostStatus.PENDING, pageable);
    }

    /**
     * 分页查询被拒绝的帖子
     */
    public Page<HelpPost> findRejectedPage(Pageable pageable) {
        return helpPostRepository.findByStatus(PostStatus.REJECTED, pageable);
    }

    /**
     * 分页查询审核通过且与指定宠物类型相关的帖子
     * 用于在宠物详情页展示相关讨论
     */
    public Page<HelpPost> findApprovedByType(String petType, Pageable pageable) {
        return helpPostRepository.findApprovedByPetType(petType, PostStatus.APPROVED, pageable);
    }

    /**
     * 统计指定宠物类型相关的帖子数量
     */
    public long countByRelatedPetType(String petType) {
        return helpPostRepository.countByRelatedPet_TypeAndStatus(petType, PostStatus.APPROVED);
    }


    /**
     * 分页查询指定用户的帖子
     * 用于个人中心展示用户发布的互助帖
     */
    public Page<HelpPost> findByUserPage(Long userId, Pageable pageable) {
        return helpPostRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    /**
     * 根据关联的宠物ID查询帖子列表
     */
    public List<HelpPost> findByRelatedPetId(Long relatedPetId) {
        return helpPostRepository.findByRelatedPetId(relatedPetId);
    }

    /**
     * 根据ID删除帖子（包含所有关联数据：评论、收藏、点赞）
     */
    @Transactional
    public void deletePostWithRelations(Long postId) {
        // 1. 删除帖子下的所有评论
        commentService.deleteByPostId(postId);

        // 2. 删除帖子的收藏记录
        favoriteRepository.deleteByTargetTypeAndTargetId("HELP_POST", postId);

        // 3. 删除帖子的点赞记录
        likeRecordRepository.deleteByTargetTypeAndTargetId("HELP_POST", postId);

        // 4. 删除帖子本身
        helpPostRepository.deleteById(postId);
    }

}

