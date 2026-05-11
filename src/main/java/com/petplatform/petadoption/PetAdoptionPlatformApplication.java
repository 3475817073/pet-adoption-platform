package com.petplatform.petadoption;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetAdoptionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetAdoptionPlatformApplication.class, args);
    }

}