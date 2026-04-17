package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.VisitRecord;
import com.petplatform.petadoption.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 回访记录业务逻辑层
 * 负责处理领养回访记录的登记、查询及删除等核心业务
 */
@Service
@RequiredArgsConstructor
public class VisitRecordService {
    private final VisitRecordRepository visitRecordRepository;

    /**
     * 保存或更新回访记录信息
     */
    public VisitRecord save(VisitRecord visitRecord) {
        return visitRecordRepository.save(visitRecord);
    }

    /**
     * 根据领养申请 ID 查询该申请下的所有回访记录（按时间降序）
     */
    public List<VisitRecord> findByApplicationId(Long applicationId) {
        return visitRecordRepository.findByApplicationIdOrderByVisitTimeDesc(applicationId);
    }

    /**
     * 根据回访员 ID 查询其执行的所有回访记录（按时间降序）
     */
    public List<VisitRecord> findByVisitorId(Long visitorId) {
        return visitRecordRepository.findByVisitorIdOrderByVisitTimeDesc(visitorId);
    }

    /**
     * 根据 ID 查找单个回访记录git
     */
    public VisitRecord findById(Long id) {
        return visitRecordRepository.findById(id).orElse(null);
    }

    /**
     * 根据 ID 删除指定的回访记录
     */
    public void delete(Long id) {
        visitRecordRepository.deleteById(id);
    }
}
