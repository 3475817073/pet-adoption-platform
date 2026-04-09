package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.AdoptionApplicationService;
import com.petplatform.petadoption.service.PetService;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoption")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdoptionController {

    private final AdoptionApplicationService adoptionApplicationService;
    private final PetService petService;
    private final UserService userService;

    // 提交领养申请（领养者）
    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            if (username == null) {
                return ResponseEntity.badRequest().body("请先登录");
            }

            User adopter = userService.findByUsername(username);
            if (adopter == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // Deleted:// 验证是否为领养者
            // Deleted:if (adopter.getRole() != Role.ADOPTER) {
            // Deleted:    return ResponseEntity.badRequest().body("只有领养者可以提交申请");
            // Deleted:}

            Long petId = Long.valueOf(request.get("petId").toString());
            Pet pet = petService.findById(petId);

            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            if (pet.getStatus() != PetStatus.AVAILABLE) {
                return ResponseEntity.badRequest().body("该宠物已被领养");
            }

            AdoptionApplication application = new AdoptionApplication();
            application.setPet(pet);
            application.setAdopter(adopter);
            application.setReason((String) request.get("reason"));
            application.setFamilySituation((String) request.get("family"));
            application.setContact((String) request.get("contact"));
            application.setStatus(ApplicationStatus.PENDING);
            application.setApplyTime(LocalDateTime.now());

            adoptionApplicationService.save(application);

            return ResponseEntity.ok("申请提交成功！请等待管理员审核");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("提交失败：" + e.getMessage());
        }
    }

    // 查询我的申请（领养者）
    @GetMapping("/my-applications")
    public ResponseEntity<Page<AdoptionApplication>> getMyApplications(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
        return ResponseEntity.ok(adoptionApplicationService.findMyApplicationsPage(user.getId(), pageable));
    }


    // 查看所有申请（仅管理员）
    @GetMapping("/all")
    public ResponseEntity<?> getAllApplications(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限访问");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            Page<AdoptionApplication> applications = adoptionApplicationService.findAllPage(pageable);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    // 查看待审核申请（仅管理员）
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限访问");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "applyTime"));
            Page<AdoptionApplication> applications = adoptionApplicationService.findPendingPage(pageable);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }


    // 审核申请
    @PostMapping("/review/{applicationId}")
    public ResponseEntity<?> review(
            @PathVariable Long applicationId,
            @RequestParam String username,
            @RequestParam String action) {

        try {
            User admin = userService.findByUsername(username);
            if (admin == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            AdoptionApplication application = adoptionApplicationService.findById(applicationId);
            if (application == null) {
                return ResponseEntity.badRequest().body("申请不存在");
            }

            // 检查该申请是否已经是终态（已通过或已拒绝）
            if (application.getStatus() != ApplicationStatus.PENDING) {
                return ResponseEntity.badRequest().body("该申请已审核，不能重复操作");
            }

            if ("approve".equals(action)) {
                // 通过申请前，检查该宠物是否已有其他申请通过
                Pet pet = application.getPet();
                List<AdoptionApplication> petApplications = adoptionApplicationService.findByPetId(pet.getId());

                boolean hasApproved = petApplications.stream()
                        .anyMatch(app -> app.getStatus() == ApplicationStatus.APPROVED);

                if (hasApproved) {
                    return ResponseEntity.badRequest().body("该宠物已被其他申请通过，无法重复通过");
                }

                // 通过当前申请
                application.setStatus(ApplicationStatus.APPROVED);
                pet.setStatus(PetStatus.ADOPTED);
                petService.save(pet);

                // 自动拒绝该宠物的其他待审核申请
                for (AdoptionApplication app : petApplications) {
                    if (!app.getId().equals(applicationId) && app.getStatus() == ApplicationStatus.PENDING) {
                        app.setStatus(ApplicationStatus.REJECTED);
                        app.setReviewTime(LocalDateTime.now());
                        adoptionApplicationService.save(app);
                    }
                }

            } else if ("reject".equals(action)) {
                application.setStatus(ApplicationStatus.REJECTED);
            } else {
                return ResponseEntity.badRequest().body("无效的操作");
            }

            application.setReviewTime(LocalDateTime.now());
            adoptionApplicationService.save(application);

            return ResponseEntity.ok("审核成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败：" + e.getMessage());
        }
    }
}
