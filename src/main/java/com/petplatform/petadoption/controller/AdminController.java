package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.repository.*;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AdoptionApplicationRepository applicationRepository;
    private final HelpPostRepository helpPostRepository;
    private final UserService userService;

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest().body("无权限访问");
        }

        Map<String, Object> stats = new HashMap<>();

        // 宠物统计 - 使用数据库层面聚合查询
        long totalPets = petRepository.count();
        long catPets = petRepository.countByType("猫");
        long dogPets = petRepository.countByType("狗");

        stats.put("totalPets", totalPets);
        stats.put("pendingPets", petRepository.countByReviewStatus(PostStatus.PENDING));
        stats.put("approvedPets", petRepository.countByReviewStatus(PostStatus.APPROVED));
        stats.put("rejectedPets", petRepository.countByReviewStatus(PostStatus.REJECTED));

        // 宠物类型统计
        stats.put("catPets", catPets);
        stats.put("dogPets", dogPets);
        stats.put("otherPets", totalPets - catPets - dogPets);

        // 用户统计
        stats.put("totalUsers", userRepository.count());

        // 申请统计 - 使用数据库层面聚合查询
        stats.put("totalApplications", applicationRepository.count());
        stats.put("pendingApps", applicationRepository.countByStatus(ApplicationStatus.PENDING));
        stats.put("approvedApps", applicationRepository.countByStatus(ApplicationStatus.APPROVED));
        stats.put("rejectedApps", applicationRepository.countByStatus(ApplicationStatus.REJECTED));

        // 帖子统计 - 使用数据库层面聚合查询
        stats.put("totalPosts", helpPostRepository.count());
        stats.put("pendingPosts", helpPostRepository.countByStatus(PostStatus.PENDING));
        stats.put("approvedPosts", helpPostRepository.countByStatus(PostStatus.APPROVED));
        stats.put("rejectedPosts", helpPostRepository.countByStatus(PostStatus.REJECTED));

        return ResponseEntity.ok(stats);
    }
}
