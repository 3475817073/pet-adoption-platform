package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.AdoptionApplication;
import com.petplatform.petadoption.entity.ApplicationStatus;
import com.petplatform.petadoption.repository.AdoptionApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 领养申请业务逻辑层
 * 负责处理领养申请的保存、查询及分页检索等核心业务
 */
@Service
@RequiredArgsConstructor
public class AdoptionApplicationService {
    private final AdoptionApplicationRepository applicationRepository;

    /**
     * 保存或更新领养申请信息
     * @param application 待保存的申请实体
     * @return 保存后的申请实体
     */
    public AdoptionApplication save(AdoptionApplication application) {
        return applicationRepository.save(application);
    }

    /**
     * 根据宠物 ID 查询该宠物的所有领养申请
     * @param petId 宠物唯一标识
     * @return 申请列表
     */
    public List<AdoptionApplication> findByPetId(Long petId) {
        return applicationRepository.findByPetId(petId);
    }

    /**
     * 根据领养者 ID 查询其提交的所有申请
     * @param adopterId 领养者唯一标识
     * @return 申请列表
     */
    public List<AdoptionApplication> findByAdopterId(Long adopterId) {
        return applicationRepository.findByAdopterId(adopterId);
    }

    /**
     * 根据审核状态查询申请列表
     * @param status 申请状态（如：PENDING, APPROVED）
     * @return 符合条件的申请列表
     */
    public List<AdoptionApplication> findByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    /**
     * 查询系统中所有的领养申请
     * @return 全部申请列表
     */
    public List<AdoptionApplication> findAll() {
        return applicationRepository.findAll();
    }

    /**
     * 根据 ID 查找单个领养申请
     * @param id 申请单唯一标识
     * @return 申请实体，若不存在则返回 null
     */
    public AdoptionApplication findById(Long id) {
        return applicationRepository.findById(id).orElse(null);
    }

    /**
     * 分页查询待审核的领养申请
     * @param pageable 分页参数
     * @return 分页的待审核申请列表
     */
    public Page<AdoptionApplication> findPendingPage(Pageable pageable) {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING, pageable);
    }

    /**
     * 分页查询系统中所有的领养申请
     * @param pageable 分页参数
     * @return 分页的全部申请列表
     */
    public Page<AdoptionApplication> findAllPage(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    /**
     * 分页查询指定领养者提交的申请记录
     * @param adopterId 领养者唯一标识
     * @param pageable 分页参数
     * @return 分页的个人申请列表
     */
    public Page<AdoptionApplication> findMyApplicationsPage(Long adopterId, Pageable pageable) {
        return applicationRepository.findByAdopterId(adopterId, pageable);
    }

}
