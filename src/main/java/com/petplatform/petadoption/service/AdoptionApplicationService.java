package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.AdoptionApplication;
import com.petplatform.petadoption.entity.ApplicationStatus;
import com.petplatform.petadoption.repository.AdoptionApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionApplicationService {
    private final AdoptionApplicationRepository applicationRepository;

    public AdoptionApplication save(AdoptionApplication application) {
        return applicationRepository.save(application);
    }

    public List<AdoptionApplication> findByPetId(Long petId) {
        return applicationRepository.findByPetId(petId);
    }

    public List<AdoptionApplication> findByAdopterId(Long adopterId) {
        return applicationRepository.findByAdopterId(adopterId);
    }

    public List<AdoptionApplication> findByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<AdoptionApplication> findAll() {
        return applicationRepository.findAll();
    }

    public AdoptionApplication findById(Long id) {
        return applicationRepository.findById(id).orElse(null);
    }
}
