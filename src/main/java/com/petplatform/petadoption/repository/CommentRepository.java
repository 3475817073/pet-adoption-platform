package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreateTimeDesc(Long postId);
    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);
}
