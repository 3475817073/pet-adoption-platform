package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.repository.*;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

        // 宠物统计
        List<Pet> allPets = petRepository.findAll();
        stats.put("totalPets", allPets.size());
        stats.put("pendingPets", allPets.stream()
            .filter(p -> p.getReviewStatus() == PostStatus.PENDING).count());
        stats.put("approvedPets", allPets.stream()
            .filter(p -> p.getReviewStatus() == PostStatus.APPROVED).count());
        stats.put("rejectedPets", allPets.stream()
            .filter(p -> p.getReviewStatus() == PostStatus.REJECTED).count());

        // 宠物类型统计
        stats.put("catPets", allPets.stream()
            .filter(p -> "猫".equals(p.getType())).count());
        stats.put("dogPets", allPets.stream()
            .filter(p -> "狗".equals(p.getType())).count());
        stats.put("otherPets", allPets.stream()
            .filter(p -> !"猫".equals(p.getType()) && !"狗".equals(p.getType())).count());

        // 用户统计
        stats.put("totalUsers", userRepository.count());

        // 申请统计
        List<AdoptionApplication> allApps = applicationRepository.findAll();
        stats.put("totalApplications", allApps.size());
        stats.put("pendingApps", allApps.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.PENDING).count());
        stats.put("approvedApps", allApps.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.APPROVED).count());
        stats.put("rejectedApps", allApps.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());

        // 帖子统计
        List<HelpPost> allPosts = helpPostRepository.findAll();
        stats.put("totalPosts", allPosts.size());
        stats.put("pendingPosts", allPosts.stream()
            .filter(p -> p.getStatus() == PostStatus.PENDING).count());
        stats.put("approvedPosts", allPosts.stream()
            .filter(p -> p.getStatus() == PostStatus.APPROVED).count());
        stats.put("rejectedPosts", allPosts.stream()
            .filter(p -> p.getStatus() == PostStatus.REJECTED).count());

        return ResponseEntity.ok(stats);
    }
}
