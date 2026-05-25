package com.oriole.wisepen.skill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oriole.wisepen.skill.api.feign")
@SpringBootApplication
public class SkillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillApplication.class, args);
    }
}
