package com.fitness.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.fitness.admin")
@EnableAsync
public class FitnessAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAdminApplication.class, args);
    }
}
