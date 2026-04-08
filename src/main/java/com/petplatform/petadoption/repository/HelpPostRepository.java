package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.HelpPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HelpPostRepository extends JpaRepository<HelpPost, Long> {
    List<HelpPost> findByCategory(String category);
}