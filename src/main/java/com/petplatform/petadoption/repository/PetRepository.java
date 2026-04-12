package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//宠物数据访问层接口。继承 JpaRepository 接口，提供基本的数据访问方法
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * 根据宠物状态分页查询宠物列表
     * @param status 宠物状态（如：AVAILABLE, ADOPTED）
     * @param pageable 分页参数
     * @return 分页的宠物列表
     */
    Page<Pet> findByStatus(PetStatus status, Pageable pageable);

    /**
     * 根据宠物状态查询所有符合条件的宠物（不分页）
     * @param status 宠物状态
     * @return 宠物列表
     */
    List<Pet> findByStatus(PetStatus status);

    /**
     * 根据发布者（救助者）ID 查询其发布的所有宠物（不分页）
     * @param rescuerId 发布者唯一标识
     * @return 宠物列表
     */
    List<Pet> findByRescuerId(Long rescuerId);

    /**
     * 根据发布者（救助者）ID 分页查询其发布的宠物列表
     * @param rescuerId 发布者唯一标识
     * @param pageable 分页参数
     * @return 分页的宠物列表
     */
    Page<Pet> findByRescuerId(Long rescuerId, Pageable pageable);

    /**
     * 根据状态、种类、性别及名称进行多条件模糊查询并分页
     * @param status 宠物状态
     * @param type 种类关键词（模糊匹配）
     * @param gender 性别关键词（模糊匹配）
     * @param name 名称关键词（模糊匹配）
     * @param pageable 分页参数
     * @return 分页的宠物列表
     */
    Page<Pet> findByStatusAndTypeContainingAndGenderContainingAndNameContaining(
            PetStatus status, String type, String gender, String name, Pageable pageable);
}
