package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.AdoptionApplication;
import com.petplatform.petadoption.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    Page<AdoptionApplication> findByStatus(ApplicationStatus status, Pageable pageable);
    List<AdoptionApplication> findByPetId(Long petId);
    List<AdoptionApplication> findByAdopterId(Long adopterId);
    Page<AdoptionApplication> findByAdopterId(Long adopterId, Pageable pageable);
    List<AdoptionApplication> findByStatus(ApplicationStatus status);
}
