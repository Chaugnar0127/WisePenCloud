package com.oriole.wisepen.docs.controller;

import com.oriole.wisepen.common.core.constant.CommonConstants;
import com.oriole.wisepen.common.core.constant.SecurityConstants;
import com.oriole.wisepen.common.core.context.GrayContextHolder;
import com.oriole.wisepen.docs.config.DocsServiceProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "文档聚合", description = "代理各微服务的 OpenAPI 文档")
@RestController
@RequestMapping("/docs/api")
@RequiredArgsConstructor
public class OpenApiProxyController {

    private final LoadBalancerClient loadBalancerClient;
    private final DocsServiceProperties docsServiceProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wisepen.security.from-source:APISIX-wX0iR6tY}")
    private String fromSource;

    @Operation(summary = "获取微服务 OpenAPI 文档")
    @GetMapping(value = "/{serviceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOpenApi(@PathVariable String serviceKey) {
        String serviceName = docsServiceProperties.getServices().get(serviceKey);
        if (serviceName == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown docs service: " + serviceKey);
        }

        ServiceInstance instance = loadBalancerClient.choose(serviceName);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No instance available: " + serviceName);
        }

        URI uri = UriComponentsBuilder.fromUri(instance.getUri())
                .path("/v3/api-docs")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(SecurityConstants.HEADER_FROM_SOURCE, fromSource);
        String developerTag = GrayContextHolder.getDeveloperTag();
        if (StringUtils.hasText(developerTag)) {
            headers.set(CommonConstants.GRAY_HEADER_DEV_KEY, developerTag);
        }
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}
