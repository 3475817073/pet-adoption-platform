package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.AdoptionApplicationService;
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

    /**
     * 发布宠物信息
     * 救助者发布待领养的宠物，系统自动将宠物状态设为 AVAILABLE
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> request) {
        try {
            // 校验救助者身份
            String username = (String) request.get("username");
            User rescuer = userService.findByUsername(username);
            if (rescuer == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 构建宠物实体对象
            Pet pet = new Pet();
            pet.setName((String) request.get("name"));
            pet.setType((String) request.get("type"));
            pet.setGender((String) request.get("gender"));
            pet.setAge(Integer.valueOf(request.get("age").toString()));
            pet.setDescription((String) request.get("description"));

            // 处理宠物图片信息
            if (request.get("photoUrl") != null) {
                pet.setPhotoUrl((String) request.get("photoUrl"));
            }

            if (request.get("photoUrls") != null) {
                pet.setPhotoUrls((String) request.get("photoUrls"));
            }

            // 设置关联关系和初始状态
            pet.setRescuer(rescuer);
            pet.setStatus(PetStatus.AVAILABLE);

            petService.save(pet);

            return ResponseEntity.ok("发布成功");
        } catch (Exception e) {
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

            petService.save(pet);
            return ResponseEntity.ok("修改成功");
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



}
