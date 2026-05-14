package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import com.petplatform.petadoption.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

//宠物数据访问层接口。继承 JpaRepository 接口，提供基本的数据访问方法
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * 根据状态分页查询宠物
     */
    Page<Pet> findByStatus(PetStatus status, Pageable pageable);

    /**
     * 根据状态查询所有宠物记录
     */
    List<Pet> findByStatus(PetStatus status);

    /**
     * 根据发布者 ID 查询其发布的所有宠物记录
     */
    List<Pet> findByRescuerId(Long rescuerId);

    /**
     * 根据发布者 ID 分页查询其发布的宠物记录
     */
    Page<Pet> findByRescuerId(Long rescuerId, Pageable pageable);

    /**
     * 根据状态、类型、性别、名称分页查询宠物
     */
    Page<Pet> findByStatusAndTypeContainingAndGenderContainingAndNameContaining(
            PetStatus status, String type, String gender, String name, Pageable pageable);

    /**
     * 根据审核状态分页查询宠物
     */
    Page<Pet> findByReviewStatus(PostStatus reviewStatus, Pageable pageable);

    /**
     * 根据审核状态、状态、类型、性别、名称分页查询宠物
     */
    Page<Pet> findByReviewStatusAndStatusAndTypeContainingAndGenderContainingAndNameContaining(
            PostStatus reviewStatus, PetStatus status, String type, String gender, String name, Pageable pageable);

    /**
     * 根据审核状态查询宠物数量
     */
    long countByReviewStatus(PostStatus reviewStatus);

    /**
     * 根据类型查询宠物数量
     */
    long countByType(String type);

    /**
     * 根据类型查询非该类型的宠物数量
     */
    long countByTypeNot(String type);
}
