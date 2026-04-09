package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.service.PetService;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService petService;
    private final UserService userService;

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User rescuer = userService.findByUsername(username);
            if (rescuer == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Pet pet = new Pet();
            pet.setName((String) request.get("name"));
            pet.setType((String) request.get("type"));
            pet.setGender((String) request.get("gender"));
            pet.setAge(Integer.valueOf(request.get("age").toString()));
            pet.setDescription((String) request.get("description"));

            if (request.get("photoUrl") != null) {
                pet.setPhotoUrl((String) request.get("photoUrl"));
            }

            if (request.get("photoUrls") != null) {
                pet.setPhotoUrls((String) request.get("photoUrls"));
            }

            pet.setRescuer(rescuer);
            pet.setStatus(PetStatus.AVAILABLE);

            petService.save(pet);

            return ResponseEntity.ok("发布成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发布失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Pet>> listAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return ResponseEntity.ok(petService.findAvailablePage(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Pet> getById(@PathVariable Long id) {
        Pet pet = petService.findById(id);
        return pet != null ? ResponseEntity.ok(pet) : ResponseEntity.notFound().build();
    }

    @GetMapping("/my-pets")
    public ResponseEntity<Page<Pet>> getMyPets(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        User rescuer = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return ResponseEntity.ok(petService.findMyPetsPage(rescuer.getId(), pageable));
    }


    // 编辑宠物信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Pet pet = petService.findById(id);
            if (pet == null) {
                return ResponseEntity.badRequest().body("宠物不存在");
            }

            // 验证是否是宠物的发布者
            if (!pet.getRescuer().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("无权限修改");
            }

            // 更新字段
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

    // 删除宠物
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

            // 验证是否是宠物的发布者
            if (!pet.getRescuer().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("无权限删除");
            }

            petService.delete(pet);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }



}