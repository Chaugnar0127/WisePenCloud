package com.oriole.wisepen.extension.fudan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FudanExtensionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FudanExtensionApplication.class, args);
    }
}