package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {
    List<VisitRecord> findByApplicationIdOrderByVisitTimeDesc(Long applicationId);
    List<VisitRecord> findByVisitorIdOrderByVisitTimeDesc(Long visitorId);
}
