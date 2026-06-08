package com.fitness.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.fitness.admin")
@EnableAsync
@EnableScheduling
public class FitnessAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAdminApplication.class, args);
    }
}
