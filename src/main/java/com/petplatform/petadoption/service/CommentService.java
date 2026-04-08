package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Comment;
import com.petplatform.petadoption.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreateTimeDesc(postId);
    }

    public List<Comment> findByParentId(Long parentId) {
        return commentRepository.findByParentIdOrderByCreateTimeAsc(parentId);
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }
}
