package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.repository.HelpPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 互助帖子业务逻辑层
 * 负责处理互助帖子的发布、查询及分页检索等核心业务
 */
@Service
@RequiredArgsConstructor
public class HelpPostService {
    private final HelpPostRepository helpPostRepository;

    /**
     * 保存或更新互助帖子信息
     * @param helpPost 待保存的帖子实体
     * @return 保存后的帖子实体
     */
    public HelpPost save(HelpPost helpPost) {
        return helpPostRepository.save(helpPost);
    }

    /**
     * 查询系统中所有的互助帖子
     * @return 全部帖子列表
     */
    public List<HelpPost> findAll() {
        return helpPostRepository.findAll();
    }

    /**
     * 根据 ID 查找单个互助帖子
     * @param id 帖子唯一标识
     * @return 帖子实体，若不存在则返回 null
     */
    public HelpPost findById(Long id) {
        return helpPostRepository.findById(id).orElse(null);
    }

    /**
     * 分页查询互助帖子列表
     * @param pageable 分页参数
     * @return 分页的帖子列表
     */
    public Page<HelpPost> findPage(Pageable pageable) {
        return helpPostRepository.findAll(pageable);
    }

}
