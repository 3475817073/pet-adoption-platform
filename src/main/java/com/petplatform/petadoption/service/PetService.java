package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import com.petplatform.petadoption.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 宠物业务逻辑层
 * 负责处理宠物信息的发布、查询、筛选及分页检索等核心业务
 */
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;

    /**
     * 保存或更新宠物信息
     * @param pet 待保存的宠物实体
     * @return 保存后的宠物实体
     */
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    /**
     * 查询所有处于可领养状态的宠物列表
     * @return 可领养宠物列表
     */
    public List<Pet> findAllAvailable() {
        return petRepository.findByStatus(PetStatus.AVAILABLE);
    }

    /**
     * 根据发布者（救助者）ID 查询其发布的宠物列表
     * @param rescuerId 发布者唯一标识
     * @return 宠物列表
     */
    public List<Pet> findByRescuerId(Long rescuerId) {
        return petRepository.findByRescuerId(rescuerId);
    }

    /**
     * 根据 ID 查找单个宠物信息
     * @param id 宠物唯一标识
     * @return 宠物实体，若不存在则返回 null
     */
    public Pet findById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    /**
     * 删除指定的宠物记录
     * @param pet 待删除的宠物实体
     */
    public void delete(Pet pet) {
        petRepository.delete(pet);
    }

    /**
     * 分页查询处于可领养状态的宠物列表
     * @param pageable 分页参数
     * @return 分页的可领养宠物列表
     */
    public Page<Pet> findAvailablePage(Pageable pageable) {
        return petRepository.findByStatus(PetStatus.AVAILABLE, pageable);
    }

    /**
     * 分页查询指定发布者（救助者）发布的宠物列表
     * @param rescuerId 发布者唯一标识
     * @param pageable 分页参数
     * @return 分页的宠物列表
     */
    public Page<Pet> findMyPetsPage(Long rescuerId, Pageable pageable) {
        return petRepository.findByRescuerId(rescuerId, pageable);
    }

    /**
     * 多条件筛选并分页查询可领养宠物
     * 支持按种类、性别、名称进行模糊匹配，并按年龄范围进行过滤
     * @param type 种类关键词
     * @param gender 性别关键词
     * @param ageMin 最小年龄
     * @param ageMax 最大年龄
     * @param name 名称关键词
     * @param pageable 分页参数
     * @return 分页的筛选结果
     */
    public Page<Pet> findFilteredAvailablePage(String type, String gender, Integer ageMin, Integer ageMax, String name, Pageable pageable) {
        /*
         * 首先利用 Repository 的方法对种类、性别和名称进行数据库层面的模糊查询
         */
        Page<Pet> page = petRepository.findByStatusAndTypeContainingAndGenderContainingAndNameContaining(
                PetStatus.AVAILABLE,
                type != null ? type : "",
                gender != null ? gender : "",
                name != null ? name : "",
                pageable);

        /*
         * 由于 JPA 方法名查询对数值范围支持有限，此处采用内存流式处理对年龄进行二次过滤
         */
        if (ageMin != null || ageMax != null) {
            List<Pet> filtered = page.getContent().stream()
                    .filter(pet -> {
                        int age = pet.getAge() != null ? pet.getAge() : 0;
                        if (ageMin != null && age < ageMin) return false;
                        if (ageMax != null && age > ageMax) return false;
                        return true;
                    })
                    .toList();

            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, page.getTotalElements());
        }

        return page;
    }

}
