package com.oriole.wisepen.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oriole.wisepen")
@EnableScheduling
@SpringBootApplication
public class ResPermissionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResPermissionApplication.class, args);
    }
}