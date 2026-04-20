package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.AdoptionApplication;
import com.petplatform.petadoption.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


//领养申请数据访问层接口，继承JpaRepository接口
public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {

    /**
     * 根据状态分页查询领养申请
     */
    Page<AdoptionApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * 根据宠物 ID 查询该宠物的所有申请记录
     */
    List<AdoptionApplication> findByPetId(Long petId);

    /**
     * 根据领养者 ID 查询其提交的所有申请
     */
    List<AdoptionApplication> findByAdopterId(Long adopterId);

    /**
     * 根据领养者 ID 分页查询其提交的申请记录
     */
    Page<AdoptionApplication> findByAdopterId(Long adopterId, Pageable pageable);

    /**
     * 根据状态查询所有申请记录
     */
    List<AdoptionApplication> findByStatus(ApplicationStatus status);
}
