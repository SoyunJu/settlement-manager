package com.settlement.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SettlementManagerApplication {

    public static void main(String[] args) {

        System.out.println("=== WORKING DIR: " + System.getProperty("user.dir"));
        System.out.println("=== JWT_SECRET: " + System.getenv("JWT_SECRET"));

        SpringApplication.run(SettlementManagerApplication.class, args);
    }

}