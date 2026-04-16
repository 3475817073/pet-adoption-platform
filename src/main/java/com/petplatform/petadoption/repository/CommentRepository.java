package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//评论数据访问层接口，用于操作数据库中的评论数据
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据帖子 ID 查询该帖子下的所有评论，并按创建时间降序排列
     */
    List<Comment> findByPostIdOrderByCreateTimeDesc(Long postId);

    /**
     * 根据父级评论 ID 查询其下的所有回复，并按创建时间升序排列
     */
    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);
}
