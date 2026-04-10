package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.VisitRecord;
import com.petplatform.petadoption.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitRecordService {
    private final VisitRecordRepository visitRecordRepository;

    public VisitRecord save(VisitRecord visitRecord) {
        return visitRecordRepository.save(visitRecord);
    }

    public List<VisitRecord> findByApplicationId(Long applicationId) {
        return visitRecordRepository.findByApplicationIdOrderByVisitTimeDesc(applicationId);
    }

    public List<VisitRecord> findByVisitorId(Long visitorId) {
        return visitRecordRepository.findByVisitorIdOrderByVisitTimeDesc(visitorId);
    }

    public VisitRecord findById(Long id) {
        return visitRecordRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        visitRecordRepository.deleteById(id);
    }
}
