package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.repository.HelpPostRepository;
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
}
