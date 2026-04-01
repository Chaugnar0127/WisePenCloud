package com.oriole.wisepen.extension.fudan.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.oriole.wisepen.extension.fudan.constant.MqTopicConstants;
import com.oriole.wisepen.extension.fudan.domain.dto.FudanUISTaskResultDTO;
import com.oriole.wisepen.extension.fudan.domain.mq.FudanUISAuthRequestMessage;
import com.oriole.wisepen.extension.fudan.enums.FudanUISTaskState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oriole.wisepen.extension.fudan.cache.RedisCacheManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class UISAuthRequestConsumer {

    private final RedisCacheManager redisCacheManager;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = MqTopicConstants.FUDAN_UIS_AUTH_REQ, groupId = "wisepen-extension-fudan-uis-auth-group")
    public void handleUisAuthRequest(String payload) throws JsonProcessingException {
        FudanUISAuthRequestMessage msg = objectMapper.readValue(payload, FudanUISAuthRequestMessage.class);
        runScraper(msg.getUserId(), msg.getAccount(), msg.getPassword());
    }

    private void runScraper(Long uid, String account, String password) {
        redisCacheManager.setUisTaskStatus(uid, FudanUISTaskResultDTO.of(FudanUISTaskState.PENDING));

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true).setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage")));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://my.fudan.edu.cn/");
            page.locator("#login-username").fill(account);
            page.locator("#login-password").fill(password);

            Response qrResponse = page.waitForResponse(
                    response -> response.url().contains("getAuthQr") && response.status() == 200,
                    () -> page.locator(".content_submit").click()
            );

            String base64Image = extractQrBase64(qrResponse.text());
            if (base64Image == null) {
                redisCacheManager.setUisTaskStatus(uid, FudanUISTaskResultDTO.of(FudanUISTaskState.FAILED_AUTH));
                return;
            }

            FudanUISTaskResultDTO waitingDto = FudanUISTaskResultDTO.of(FudanUISTaskState.WAITING_SCAN);
            waitingDto.setQrBase64(base64Image);
            redisCacheManager.setUisTaskStatus(uid, waitingDto);

            try {
                page.waitForURL(Pattern.compile(".*my\\.fudan\\.edu\\.cn.*"),
                        new Page.WaitForURLOptions().setTimeout(120_000));

                page.waitForLoadState(LoadState.NETWORKIDLE);
                Map<String, String> infoMap = parseStudentInfo(page);
                infoMap.put("学号", account);

                FudanUISTaskResultDTO successDto = FudanUISTaskResultDTO.of(FudanUISTaskState.SUCCESS);
                successDto.setProfile(infoMap);
                redisCacheManager.setUisTaskStatus(uid, successDto);

            } catch (TimeoutError e) {
                log.error("UID {} UIS认证超时", uid, e);
                redisCacheManager.setUisTaskStatus(uid, FudanUISTaskResultDTO.of(FudanUISTaskState.FAILED_TIMEOUT));
            }

        } catch (Exception e) {
            log.error("UID {} UIS认证异常", uid, e);
            redisCacheManager.setUisTaskStatus(uid, FudanUISTaskResultDTO.of(FudanUISTaskState.FAILED_ERROR));
        }
    }

    /**
     * 利用正则提取 qrMsg 中的纯 Base64 图片数据
     */
    private String extractQrBase64(String json) {
        Pattern p = Pattern.compile("\"qrMsg\"\\s*:\\s*\"data:image/png;base64,([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 解析登录成功后页面上的学生信息表格
     */
    private Map<String, String> parseStudentInfo(Page page) {
        Map<String, String> infoMap = new LinkedHashMap<>();

        // 定位表格中的所有单元格
        // 选择器策略：找到 .card-body 下的 .table 里的所有 td
        Locator cells = page.locator(".card-body .table td");
        int count = cells.count();

        // 步长为 2，因为数据是 Key-Value 成对出现的
        for (int i = 0; i < count; i += 2) {
            // 提取 Key (移除冒号和多余空白)
            String key = cells.nth(i).innerText().trim().replace("：", "");
            // 提取 Value
            String value = cells.nth(i + 1).innerText().trim();
            infoMap.put(key, value);
        }

        return infoMap;
    }
}