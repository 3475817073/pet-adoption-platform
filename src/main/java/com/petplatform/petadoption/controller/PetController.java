package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.AdoptionApplicationService;
import com.petplatform.petadoption.service.HelpPostService;
import com.petplatform.petadoption.service.PetService;
import com.petplatform.petadoption.service.UserService;
import com.petplatform.petadoption.service.VisitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * 宠物管理控制器
 * 提供宠物信息的发布、查询、修改、删除等 RESTful API 接口
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService petService;
    private final UserService userService;
    private final AdoptionApplicationService adoptionApplicationService;
    private final VisitRecordService visitRecordService;
    private final HelpPostService helpPostService;

    /**
     * 发布宠物信息
     * 救助者发布待领养的宠物，系统自动将宠物状态设为 AVAILABLE
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> request) {
        try {
            // 校验救助者身份
            String username = (String) request.get("username");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }

            User rescuer = userService.findByUsername(username);
            if (rescuer == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 校验必填字段
            if (request.get("name") == null || ((String) request.get("name")).isEmpty()) {
                return ResponseEntity.badRequest().body("宠物名称不能为空");
            }
            if (request.get("type") == null || ((String) request.get("type")).isEmpty()) {
                return ResponseEntity.badRequest().body("宠物种类不能为空");
            }
            if (request.get("description") == null || ((String) request.get("description")).isEmpty()) {
                return ResponseEntity.badRequest().body("宠物描述不能为空");
            }

            // 构建宠物实体对象
            Pet pet = new Pet();
            pet.setName((String) request.get("name"));
            pet.setType((String) request.get("type"));
            pet.setGender((String) request.get("gender"));

            // 处理年龄字段（支持字符串和数字类型）
            Object ageObj = request.get("age");
            if (ageObj != null) {
                pet.setAge(Integer.valueOf(ageObj.toString()));
            } else {
                pet.setAge(1); // 默认年龄
            }

            pet.setDescription((String) request.get("description"));

            // 处理宠物图片信息
            if (request.get("photoUrl") != null) {
                pet.setPhotoUrl((String) request.get("photoUrl"));
            }

            // 处理图片列表信息
            if (request.get("photoUrls") != null) {
                pet.setPhotoUrls((String) request.get("photoUrls"));
            }
            // 处理标签信息
            if (request.get("tags") != null) {
                pet.setTags((String) request.get("tags"));
            }

            // 处理健康状态（支持Boolean和String类型）
            Object vaccinatedObj = request.get("isVaccinated");
            if (vaccinatedObj != null) {
                pet.setVaccinated(Boolean.valueOf(vaccinatedObj.toString()));
            }

            Object neuteredObj = request.get("isNeutered");
            if (neuteredObj != null) {
                pet.setNeutered(Boolean.valueOf(neuteredObj.toString()));
            }

            Object dewormedObj = request.get("isDewormed");
            if (dewormedObj != null) {
                pet.setDewormed(Boolean.valueOf(dewormedObj.toString()));
            }


            // 设置关联关系和初始状态
            pet.setRescuer(rescuer);
            pet.setStatus(PetStatus.AVAILABLE);
            pet.setReviewStatus(com.petplatform.petadoption.entity.PostStatus.PENDING);

            petService.save(pet);

            return ResponseEntity.ok("发布成功，请等待管理员审核");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("发布宠物失败，请求数据：" + request);
            return ResponseEntity.badRequest().body("发布失败：" + e.getMessage());
        }
    }



    /**
     * 根据 ID 查询宠物详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getById(@PathVariable Long id) {
        Pet pet = petService.findById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    /**
     * 查询当前用户发布的宠物列表（分页）
     * 用于个人中心展示救助者自己发布的宠物
     */
    @GetMapping("/my-pets")
    public ResponseEntity<Page<Pet>> getMyPets(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        User rescuer = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return ResponseEntity.ok(petService.findMyPetsPage(rescuer.getId(), pageable));
    }

    /**
     * 编辑宠物信息
     * 仅允许宠物发布者本人修改信息，确保数据安全
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            // 校验操作者身份
            String username = (String) request.get("username");
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 校验宠物是否存在
            Pet pet = petService.findById(id);
            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            // 权限校验：仅发布者有权修改
            if (!pet.getRescuer().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("无权限修改");
            }

            // 按需更新宠物各属性
            if (request.get("name") != null) pet.setName((String) request.get("name"));
            if (request.get("type") != null) pet.setType((String) request.get("type"));
            if (request.get("gender") != null) pet.setGender((String) request.get("gender"));
            if (request.get("age") != null) pet.setAge(Integer.valueOf(request.get("age").toString()));
            if (request.get("description") != null) pet.setDescription((String) request.get("description"));
            if (request.get("photoUrl") != null) pet.setPhotoUrl((String) request.get("photoUrl"));
            if (request.get("photoUrls") != null) pet.setPhotoUrls((String) request.get("photoUrls"));

            // 修改后需要重新审核
            if (pet.getReviewStatus() == com.petplatform.petadoption.entity.PostStatus.APPROVED) {
                pet.setReviewStatus(com.petplatform.petadoption.entity.PostStatus.PENDING);
            }

            petService.save(pet);
            return ResponseEntity.ok("修改成功，请等待管理员重新审核");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除宠物信息
     * 仅允许宠物发布者本人删除，防止误操作
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(
            @PathVariable Long id,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Pet pet = petService.findById(id);
            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            // 权限校验：只有发布者或管理员可以删除
            if (!pet.getRescuer().getId().equals(user.getId()) && user.getRole() != com.petplatform.petadoption.entity.Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限删除");
            }

            // 先删除关联的申请和回访记录
            List<AdoptionApplication> applications = adoptionApplicationService.findByPetId(id);
            for (AdoptionApplication app : applications) {
                // 1. 先删该申请下的所有回访记录
                List<VisitRecord> visits = visitRecordService.findByApplicationId(app.getId());
                for (VisitRecord visit : visits) {
                    visitRecordService.delete(visit.getId());
                }
                // 2. 再删申请本身
                adoptionApplicationService.delete(app.getId());
            }

            // 3. 最后删除宠物
            petService.delete(pet);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取可领养宠物列表（支持多条件筛选与分页）
     * 供前端宠物列表页面展示，支持按种类、性别、年龄段和名称模糊搜索
     */
    @GetMapping("/list")
    public ResponseEntity<Page<Pet>> listAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 解析年龄段筛选参数
        Integer ageMin = null, ageMax = null;
        if ("young".equals(age)) {
            ageMax = 0;
        } else if ("adult".equals(age)) {
            ageMin = 1;
            ageMax = 3;
        } else if ("senior".equals(age)) {
            ageMin = 4;
        }

        return ResponseEntity.ok(petService.findFilteredAvailablePage(type, gender, ageMin, ageMax, name, pageable));
    }


    /**
     * 获取待审核的宠物列表（仅限管理员，分页）
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPets(
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
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createTime"));
            Page<Pet> pets = petService.findPendingPage(pageable);
            return ResponseEntity.ok(pets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取已拒绝的宠物列表（仅限管理员，分页）
     */
    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedPets(
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
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<Pet> pets = petService.findRejectedPage(pageable);
            return ResponseEntity.ok(pets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 审核宠物
     */
    @PostMapping("/review/{petId}")
    public ResponseEntity<?> reviewPet(
            @PathVariable Long petId,
            @RequestParam String username,
            @RequestParam String action,
            @RequestParam(required = false) String reason) {
        try {
            User admin = userService.findByUsername(username);
            if (admin == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (admin.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限操作");
            }

            Pet pet = petService.findById(petId);
            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            if (pet.getReviewStatus() != com.petplatform.petadoption.entity.PostStatus.PENDING) {
                return ResponseEntity.badRequest().body("该宠物已审核，不能重复操作");
            }

            if ("approve".equals(action)) {
                pet.setReviewStatus(com.petplatform.petadoption.entity.PostStatus.APPROVED);
            } else if ("reject".equals(action)) {
                pet.setReviewStatus(com.petplatform.petadoption.entity.PostStatus.REJECTED);
                if (reason != null && !reason.trim().isEmpty()) {
                    pet.setRejectReason(reason);
                }
            } else {
                return ResponseEntity.badRequest().body("无效的操作");
            }

            petService.save(pet);
            return ResponseEntity.ok("审核成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败：" + e.getMessage());
        }
    }

    /**
     * 获取与某只宠物相关的讨论帖子（前3条）
     * 用于宠物详情页底部展示社区讨论
     */
    @GetMapping("/{id}/related-posts")
    public ResponseEntity<?> getRelatedPosts(@PathVariable Long id) {
        try {
            Pet pet = petService.findById(id);
            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<HelpPost> posts = helpPostService.findApprovedByType(pet.getType(), pageable);

            return ResponseEntity.ok(posts.getContent());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

}
