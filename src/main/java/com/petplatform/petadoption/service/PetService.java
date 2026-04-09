package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import com.petplatform.petadoption.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;

    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    public List<Pet> findAllAvailable() {
        return petRepository.findByStatus(PetStatus.AVAILABLE);
    }

    public List<Pet> findByRescuerId(Long rescuerId) {
        return petRepository.findByRescuerId(rescuerId);
    }

    public Pet findById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    public void delete(Pet pet) {
        petRepository.delete(pet);
    }

    public Page<Pet> findAvailablePage(Pageable pageable) {
        return petRepository.findByStatus(PetStatus.AVAILABLE, pageable);
    }

    public Page<Pet> findMyPetsPage(Long rescuerId, Pageable pageable) {
        return petRepository.findByRescuerId(rescuerId, pageable);
    }

}
