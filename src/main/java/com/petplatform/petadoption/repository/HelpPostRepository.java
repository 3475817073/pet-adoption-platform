package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}