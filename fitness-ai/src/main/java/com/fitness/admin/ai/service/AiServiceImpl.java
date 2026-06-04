package com.fitness.admin.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fitness.admin.ai.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI服务实现 - 支持OpenAI、Claude、DeepSeek
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String chat(List<ChatMessage> messages) {
        try {
            String provider = aiConfig.getProvider().toLowerCase();
            switch (provider) {
                case "claude":
                    return callClaudeApi(messages);
                case "deepseek":
                    return callDeepSeekApi(messages);
                case "openai":
                default:
                    return callOpenAiApi(messages);
            }
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public String chat(String userMessage) {
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", aiConfig.getSystemPrompt()),
                new ChatMessage("user", userMessage)
        );
        return chat(messages);
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAiApi(List<ChatMessage> messages) throws IOException {
        String url = aiConfig.getApiBaseUrl() + "/chat/completions";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());
        requestBody.put("temperature", aiConfig.getTemperature());

        ArrayNode messagesArray = requestBody.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", msg.getRole());
            messageNode.put("content", msg.getContent());
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("OpenAI API调用失败: {}", errorBody);
                throw new RuntimeException("AI服务调用失败: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }

    /**
     * 调用Claude API
     */
    private String callClaudeApi(List<ChatMessage> messages) throws IOException {
        String url = aiConfig.getApiBaseUrl();

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        // 提取系统消息
        String systemMessage = aiConfig.getSystemPrompt();
        ArrayNode messagesArray = requestBody.putArray("messages");

        for (ChatMessage msg : messages) {
            if ("system".equals(msg.getRole())) {
                systemMessage = msg.getContent();
            } else {
                ObjectNode messageNode = messagesArray.addObject();
                messageNode.put("role", msg.getRole());
                messageNode.put("content", msg.getContent());
            }
        }

        if (systemMessage != null) {
            requestBody.put("system", systemMessage);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", aiConfig.getApiKey())
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Claude API调用失败: {}", errorBody);
                throw new RuntimeException("AI服务调用失败: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("content").get(0).get("text").asText();
        }
    }

    /**
     * 调用DeepSeek API (兼容OpenAI格式)
     */
    private String callDeepSeekApi(List<ChatMessage> messages) throws IOException {
        String url = aiConfig.getApiBaseUrl() + "/chat/completions";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());
        requestBody.put("temperature", aiConfig.getTemperature());

        ArrayNode messagesArray = requestBody.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", msg.getRole());
            messageNode.put("content", msg.getContent());
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("DeepSeek API调用失败: {}", errorBody);
                throw new RuntimeException("AI服务调用失败: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }
}
