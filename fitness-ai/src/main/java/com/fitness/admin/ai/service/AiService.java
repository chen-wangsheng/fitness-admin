package com.fitness.admin.ai.service;

import java.util.List;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 发送聊天消息并获取响应
     *
     * @param messages 消息列表，包含角色和内容
     * @return AI响应内容
     */
    String chat(List<ChatMessage> messages);

    /**
     * 单轮对话
     *
     * @param userMessage 用户消息
     * @return AI响应内容
     */
    String chat(String userMessage);

    /**
     * 消息类
     */
    class ChatMessage {
        private String role; // system, user, assistant
        private String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
