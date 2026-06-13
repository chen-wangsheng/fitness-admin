package com.fitness.admin.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * AI提供商：openai, claude, deepseek
     */
    private String provider = "openai";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API Base URL
     */
    private String apiBaseUrl = "https://api.openai.com/v1";

    /**
     * 模型名称
     */
    private String model = "gpt-3.5-turbo";

    /**
     * 最大token数
     */
    private Integer maxTokens = 2000;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * AI 调用超时(秒),超过此时间未返回首字节则中断。
     * 默认为 60s,可被 ai.timeout-seconds 环境变量覆盖。
     */
    private Integer timeoutSeconds = 60;

    /**
     * 单用户每分钟最大调用次数(限流)。0 表示不限制。
     */
    private Integer rateLimitPerMinute = 20;

    /**
     * 系统提示词
     */
    private String systemPrompt = "你是一个专业的AI健身助手，名叫FitBot。你擅长：\n" +
            "1. 制定个性化的训练计划\n" +
            "2. 解答健身动作和技巧问题\n" +
            "3. 提供营养和饮食建议\n" +
            "4. 分析训练数据和进展\n" +
            "5. 提供运动伤害预防建议\n\n" +
            "请用友好、专业的语气回答用户的问题。如果用户问到非健身相关的问题，礼貌地引导他们回到健身话题。";
}
