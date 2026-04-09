package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.repository.HelpPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class HelpPostService {
    private final HelpPostRepository helpPostRepository;

    public HelpPost save(HelpPost helpPost) {
        return helpPostRepository.save(helpPost);
    }

    public List<HelpPost> findAll() {
        return helpPostRepository.findAll();
    }

    public HelpPost findById(Long id) {
        return helpPostRepository.findById(id).orElse(null);
    }

    public Page<HelpPost> findPage(Pageable pageable) {
        return helpPostRepository.findAll(pageable);
    }

}
