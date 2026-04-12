package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
//回访记录数据访问层接口
public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    /**
     * 根据领养申请 ID 查询该申请下的所有回访记录，并按回访时间降序排列
     * @param applicationId 领养申请唯一标识
     * @return 回访记录列表
     */
    List<VisitRecord> findByApplicationIdOrderByVisitTimeDesc(Long applicationId);

    /**
     * 根据回访员（用户）ID 查询其执行的所有回访记录，并按回访时间降序排列
     * @param visitorId 回访员唯一标识
     * @return 回访记录列表
     */
    List<VisitRecord> findByVisitorIdOrderByVisitTimeDesc(Long visitorId);
}
