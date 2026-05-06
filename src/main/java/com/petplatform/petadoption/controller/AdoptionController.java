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

/**
 * 领养申请控制器
 * 处理领养申请的提交、查询（个人/管理员）、审核等核心业务逻辑
 */
@RestController
@RequestMapping("/api/adoption")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdoptionController {

    private final AdoptionApplicationService adoptionApplicationService;
    private final PetService petService;
    private final UserService userService;

    /**
     * 提交领养申请
     */
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


            Long petId = Long.valueOf(request.get("petId").toString());
            Pet pet = petService.findById(petId);

            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            if (pet.getStatus() != PetStatus.AVAILABLE) {
                return ResponseEntity.badRequest().body("该宠物已被领养");
            }

            /*
             * 构建申请实体，设置基础信息及扩展字段（居住类型、养宠经验等）
             */
            AdoptionApplication application = new AdoptionApplication();
            application.setPet(pet);
            application.setAdopter(adopter);
            application.setReason((String) request.get("reason"));
            application.setFamilySituation((String) request.get("family"));
            application.setContact((String) request.get("contact"));
            application.setStatus(ApplicationStatus.PENDING);
            application.setApplyTime(LocalDateTime.now());

            // 新增字段
            application.setResidenceType((String) request.get("residenceType"));
            application.setHousingArea(request.get("housingArea") != null
                    ? Integer.valueOf(request.get("housingArea").toString())
                    : null);
            application.setPetExperience((String) request.get("petExperience"));
            application.setHasOtherPets(Boolean.valueOf(request.get("hasOtherPets").toString()));
            application.setOtherPetsInfo((String) request.get("otherPetsInfo"));


            adoptionApplicationService.save(application);

            return ResponseEntity.ok("申请提交成功！请等待管理员审核");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户提交的领养申请列表（分页）
     */
    @GetMapping("/my-applications")
    public ResponseEntity<Page<AdoptionApplication>> getMyApplications(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
        return ResponseEntity.ok(adoptionApplicationService.findMyApplicationsPage(user.getId(), pageable));
    }


    /**
     * 查看所有领养申请（仅限管理员，分页）
     */
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

    /**
     * 查看待审核的领养申请（仅限管理员，分页）
     */
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

    /**
     * 查看已通过的领养申请（仅限管理员，分页）
     */
    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedApplications(
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
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewTime"));
            Page<AdoptionApplication> applications = adoptionApplicationService.findApprovedPage(pageable);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查看已拒绝的领养申请（仅限管理员，分页）
     */
    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedApplications(
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
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewTime"));
            Page<AdoptionApplication> applications = adoptionApplicationService.findRejectedPage(pageable);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }


    /**
     * 审核领养申请（通过或拒绝）
     * 若通过申请，则自动将宠物状态更为“已领养”并拒绝该宠物其他待审申请
     */
    @PostMapping("/review/{applicationId}")
    public ResponseEntity<?> review(
            @PathVariable Long applicationId,
            @RequestParam String username,
            @RequestParam String action,
            @RequestParam(required = false) String reason) {

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
                /*
                 * 通过申请逻辑：校验宠物是否已被领养，更新宠物状态，并批量拒绝同宠物的其他待审申请
                 */
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
                        app.setRejectReason("该宠物已被其他用户领养，感谢您的关注！");
                        app.setReviewTime(LocalDateTime.now());
                        adoptionApplicationService.save(app);
                    }
                }

            } else if ("reject".equals(action)) {
                application.setStatus(ApplicationStatus.REJECTED);
                if (reason != null && !reason.trim().isEmpty()) {
                    application.setRejectReason(reason);
                }
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
