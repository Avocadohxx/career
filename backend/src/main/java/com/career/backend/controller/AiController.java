package com.career.backend.controller;

import com.career.backend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${doubao.api-key:}")
    private String apiKey;

    @Value("${doubao.model:doubao-1-5-pro-256k-250115}")
    private String model;

    @Value("${doubao.endpoint:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, Object> body) {
        if (apiKey == null || apiKey.isBlank()) {
            return Result.error("AI 服务未配置，请联系管理员设置 API Key");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        body.put("model", model);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, request, Map.class);
            return Result.success(response.getBody());
        } catch (Exception e) {
            return Result.error("AI 服务调用失败：" + e.getMessage());
        }
    }
}
