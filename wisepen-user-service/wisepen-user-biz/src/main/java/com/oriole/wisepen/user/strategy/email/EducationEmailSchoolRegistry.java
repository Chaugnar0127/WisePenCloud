package com.oriole.wisepen.user.strategy.email;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class EducationEmailSchoolRegistry {

    private static final String DATA_RESOURCE = "data/edu-email-schools.json";
    private static final Set<String> TARGET_SUFFIXES = Set.of(".edu", ".edu.cn");
    private final ObjectMapper objectMapper;
    private Map<String, EducationEmailSchool> schoolsByDomain = Collections.emptyMap();

    public EducationEmailSchoolRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void load() {
        ClassPathResource resource = new ClassPathResource(DATA_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            EducationEmailSchoolIndex index = objectMapper.readValue(inputStream, EducationEmailSchoolIndex.class);
            schoolsByDomain = normalizeSchools(index.getSchoolsByDomain());
            log.info("education email school index loaded. count={} sourceCommit={}",
                    schoolsByDomain.size(), index.getSourceCommit());
        } catch (IOException e) {
            log.error("education email school index load failed. resource={}", DATA_RESOURCE, e);
            throw new IllegalStateException("教育邮箱学校索引加载失败", e);
        }
    }

    public Optional<EducationEmailSchool> findByEmail(String email) {
        return findByDomain(extractDomain(email));
    }

    public Optional<EducationEmailSchool> findByDomain(String domain) {
        String candidateDomain = normalizeDomain(domain);
        if (!isTargetDomain(candidateDomain)) {
            return Optional.empty();
        }
        while (StrUtil.isNotBlank(candidateDomain)) {
            EducationEmailSchool school = schoolsByDomain.get(candidateDomain);
            if (school != null) {
                return Optional.of(school);
            }
            int dotIndex = candidateDomain.indexOf('.');
            if (dotIndex < 0) {
                break;
            }
            candidateDomain = candidateDomain.substring(dotIndex + 1);
        }
        return Optional.empty();
    }

    private static String extractDomain(String email) {
        if (StrUtil.isBlank(email)) {
            return "";
        }
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return "";
        }
        return email.substring(atIndex + 1);
    }

    private static String normalizeDomain(String domain) {
        if (StrUtil.isBlank(domain)) {
            return "";
        }
        return domain.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, EducationEmailSchool> normalizeSchools(Map<String, EducationEmailSchool> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, EducationEmailSchool> normalized = new HashMap<>();
        source.forEach((domain, school) -> {
            String normalizedDomain = normalizeDomain(domain);
            if (StrUtil.isBlank(normalizedDomain)
                    || !isTargetDomain(normalizedDomain)
                    || school == null
                    || StrUtil.isBlank(school.getNameZh())) {
                return;
            }
            school.setDomain(normalizedDomain);
            normalized.put(normalizedDomain, school);
        });
        return Collections.unmodifiableMap(normalized);
    }

    private static boolean isTargetDomain(String domain) {
        return TARGET_SUFFIXES.stream().anyMatch(domain::endsWith);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EducationEmailSchoolIndex {
        private String sourceCommit;
        private Map<String, EducationEmailSchool> schoolsByDomain;
    }
}