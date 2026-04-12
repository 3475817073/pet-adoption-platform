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
     * @param status 申请状态
     * @param pageable 分页参数
     * @return 分页的申请列表
     */
    Page<AdoptionApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * 根据宠物 ID 查询该宠物的所有申请记录
     * @param petId 宠物唯一标识
     * @return 申请列表
     */
    List<AdoptionApplication> findByPetId(Long petId);

    /**
     * 根据领养者 ID 查询其提交的所有申请（不分页）
     * @param adopterId 领养者唯一标识
     * @return 申请列表
     */
    List<AdoptionApplication> findByAdopterId(Long adopterId);

    /**
     * 根据领养者 ID 分页查询其提交的申请记录
     * @param adopterId 领养者唯一标识
     * @param pageable 分页参数
     * @return 分页的申请列表
     */
    Page<AdoptionApplication> findByAdopterId(Long adopterId, Pageable pageable);

    /**
     * 根据状态查询所有申请记录（不分页）
     * @param status 申请状态
     * @return 申请列表
     */
    List<AdoptionApplication> findByStatus(ApplicationStatus status);
}
