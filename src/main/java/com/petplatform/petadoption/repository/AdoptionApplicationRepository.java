package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.AdoptionApplication;
import com.petplatform.petadoption.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    List<AdoptionApplication> findByPetId(Long petId);
    List<AdoptionApplication> findByAdopterId(Long adopterId);
    List<AdoptionApplication> findByStatus(ApplicationStatus status);
}