package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
//回访记录数据访问层接口
public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    /**
     * 根据领养申请 ID 查询该申请下的所有回访记录，并按回访时间降序排列
     */
    List<VisitRecord> findByApplicationIdOrderByVisitTimeDesc(Long applicationId);

    /**
     * 根据回访员（用户）ID 查询其执行的所有回访记录，并按回访时间降序排列
     */
    List<VisitRecord> findByVisitorIdOrderByVisitTimeDesc(Long visitorId);

    /**
     * 查询指定时间范围内且类型为PLANNED的回访记录
     */
    List<VisitRecord> findByVisitTimeBetweenAndVisitType(LocalDateTime start, LocalDateTime end, String visitType);

    /**
     * 查询指定时间之前且类型为PLANNED的回访记录（超期未完成）
     */
    List<VisitRecord> findByVisitTimeBeforeAndVisitType(LocalDateTime time, String visitType);

    /**
     * 查询所有回访记录，按回访时间降序排列
     */
    List<VisitRecord> findAllByOrderByVisitTimeDesc();
}
