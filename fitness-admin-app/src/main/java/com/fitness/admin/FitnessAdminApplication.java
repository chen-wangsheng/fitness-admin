package com.fitness.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fitness.admin")
public class FitnessAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAdminApplication.class, args);
    }
}
