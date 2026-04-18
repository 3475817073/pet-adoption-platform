package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Comment;
import com.petplatform.petadoption.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论业务逻辑层
 * 负责处理互助帖子评论及回复的增删查等核心业务
 */
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    /**
     * 保存或更新评论信息
     */
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    /**
     * 根据帖子 ID 查询该帖子下的所有评论（按时间降序）
     */
    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreateTimeDesc(postId);
    }

    /**
     * 根据父级评论 ID 查询其下的所有回复（按时间升序）
     */
    public List<Comment> findByParentId(Long parentId) {
        return commentRepository.findByParentIdOrderByCreateTimeAsc(parentId);
    }

    /**
     * 根据 ID 删除指定的评论
     */
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    /**
     * 根据 ID 查找单个评论
     */
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    /**
     * 根据帖子 ID 删除该帖子下的所有评论
     */
    @Transactional
    public void deleteByPostId(Long postId) {
        commentRepository.deleteByPostId(postId);
    }

}
