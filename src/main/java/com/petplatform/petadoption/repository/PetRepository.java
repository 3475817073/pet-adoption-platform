package com.petplatform.petadoption.repository;

import com.petplatform.petadoption.entity.Pet;
import com.petplatform.petadoption.entity.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByStatus(PetStatus status);
    List<Pet> findByRescuerId(Long rescuerId);

}