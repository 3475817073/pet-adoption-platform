package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.HelpPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//互助帖子数据访问层接口，提供根据分类查询的接口
public interface HelpPostRepository extends JpaRepository<HelpPost, Long> {

    /**
     * 根据分类查询互助帖子列表
     * @param category 帖子分类（如：物资共享、医疗咨询、经验分享）
     * @return 符合分类条件的帖子列表
     */
    List<HelpPost> findByCategory(String category);
}