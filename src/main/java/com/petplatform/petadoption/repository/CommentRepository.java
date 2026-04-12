package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//评论数据访问层接口，用于操作数据库中的评论数据
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据帖子 ID 查询该帖子下的所有评论，并按创建时间降序排列
     * @param postId 帖子唯一标识
     * @return 评论列表
     */
    List<Comment> findByPostIdOrderByCreateTimeDesc(Long postId);

    /**
     * 根据父级评论 ID 查询其下的所有回复，并按创建时间升序排列
     * @param parentId 父级评论唯一标识
     * @return 回复列表
     */
    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);
}
