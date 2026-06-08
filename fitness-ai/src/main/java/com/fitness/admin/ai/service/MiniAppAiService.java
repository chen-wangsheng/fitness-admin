package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.admin.ai.dto.*;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.content.entity.PlanDay;
import com.fitness.admin.content.entity.PlanDayExercise;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.mapper.PlanDayExerciseMapper;
import com.fitness.admin.content.mapper.PlanDayMapper;
import com.fitness.admin.content.mapper.PlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 小程序AI服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppAiService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final AiPlanMapper planMapper;

    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final PlanMapper workoutPlanMapper;
    private final PlanDayMapper planDayMapper;
    private final PlanDayExerciseMapper planDayExerciseMapper;

    /**
     * 发送AI对话消息
     */
    public ChatResponse sendChatMessage(ChatRequest request) {
        Long userId = getCurrentUserId();

        // 创建或获取会话
        AiChatSession session;
        if (request.getSessionId() != null) {
            session = sessionMapper.selectById(request.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                throw new BizException("会话不存在");
            }
        } else {
            session = createNewSession(userId);
        }

        // 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        userMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 更新会话消息数
        session.setMessageCount(session.getMessageCount() + 1);
        if (session.getTitle() == null) {
            session.setTitle(request.getMessage().substring(0, Math.min(request.getMessage().length(), 50)));
        }
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 获取历史消息用于上下文
        List<AiService.ChatMessage> chatMessages = buildChatMessages(session.getId());

        // 调用AI服务获取响应
        String aiResponse;
        try {
            aiResponse = aiService.chat(chatMessages);
        } catch (Exception e) {
            log.error("AI服务调用失败，使用备用响应", e);
            aiResponse = "抱歉，AI服务暂时不可用，请稍后再试。错误信息：" + e.getMessage();
        }

        // 保存AI消息
        AiChatMessage aiMessage = new AiChatMessage();
        aiMessage.setSessionId(session.getId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiMessage.setTokenCount(aiResponse.length());
        aiMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(aiMessage);

        // 更新会话消息数
        session.setMessageCount(session.getMessageCount() + 1);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getId());
        response.setMessageId(aiMessage.getId());
        response.setContent(aiResponse);
        response.setTokenCount(aiMessage.getTokenCount());
        return response;
    }

    /**
     * 获取会话消息列表
     */
    public PageResult<AiChatMessage> getChatMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        Long userId = getCurrentUserId();

        // 验证会话归属
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BizException("会话不存在");
        }

        Page<AiChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
               .orderByAsc(AiChatMessage::getCreatedAt);
        Page<AiChatMessage> result = messageMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 获取会话列表
     */
    public PageResult<AiChatSession> getChatSessions(Integer pageNum, Integer pageSize) {
        Long userId = getCurrentUserId();

        Page<AiChatSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId)
               .orderByDesc(AiChatSession::getUpdatedAt);
        Page<AiChatSession> result = sessionMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 构建聊天消息列表（用于发送给AI服务）
     */
    private List<AiService.ChatMessage> buildChatMessages(Long sessionId) {
        List<AiService.ChatMessage> messages = new ArrayList<>();

        // 添加系统提示词
        messages.add(new AiService.ChatMessage("system",
                "你是一个专业的AI健身助手，名叫FitBot。你擅长制定训练计划、解答健身问题、提供营养建议。请用友好专业的语气回答。"));

        // 获取最近20条消息作为上下文
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
               .orderByDesc(AiChatMessage::getCreatedAt)
               .last("LIMIT 20");
        List<AiChatMessage> history = messageMapper.selectList(wrapper);

        // 反转顺序（从旧到新）
        for (int i = history.size() - 1; i >= 0; i--) {
            AiChatMessage msg = history.get(i);
            messages.add(new AiService.ChatMessage(msg.getRole(), msg.getContent()));
        }

        return messages;
    }

    /**
     * 消息反馈
     */
    public void feedback(Long sessionId, Long msgId, Integer feedback) {
        Long userId = getCurrentUserId();

        // 验证会话归属
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BizException("会话不存在");
        }

        AiChatMessage message = messageMapper.selectById(msgId);
        if (message == null || !message.getSessionId().equals(sessionId)) {
            throw new BizException("消息不存在");
        }

        message.setFeedback(feedback);
        messageMapper.updateById(message);
    }

    /**
     * 生成AI计划
     */
    @Transactional
    public GeneratePlanResponse generatePlan(GeneratePlanRequest request) {
        Long userId = getCurrentUserId();

        // 构建生成计划的提示词
        String planPrompt = buildPlanPrompt(request);

        // 调用大模型API生成计划
        String aiResponse;
        try {
            List<AiService.ChatMessage> messages = List.of(
                    new AiService.ChatMessage("system", PLAN_SYSTEM_PROMPT),
                    new AiService.ChatMessage("user", planPrompt)
            );
            aiResponse = aiService.chat(messages);
        } catch (Exception e) {
            log.error("AI计划生成失败，使用备用方案", e);
            return generateFallbackPlan(userId, request);
        }

        // 解析AI返回的JSON
        Map<String, Object> planData;
        try {
            String jsonStr = extractJson(aiResponse);
            planData = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("AI计划解析失败，原始响应: {}", aiResponse, e);
            return generateFallbackPlan(userId, request);
        }

        // 构建并保存AiPlan
        AiPlan aiPlan = new AiPlan();
        aiPlan.setUserId(userId);
        aiPlan.setPrompt(planPrompt);
        aiPlan.setResponse(aiResponse);
        aiPlan.setGoal(request.getGoal());
        aiPlan.setAvailableEquipment(request.getAvailableEquipment() != null
                ? String.join(",", request.getAvailableEquipment()) : null);
        aiPlan.setDaysPerWeek(request.getDaysPerWeek());
        aiPlan.setSplitType((String) planData.getOrDefault("splitType", "full_body"));
        aiPlan.setGenerationParams(request.getBodyMetrics() != null
                ? objectMapper.valueToTree(request.getBodyMetrics()).toString() : null);
        aiPlan.setExplanation((String) planData.getOrDefault("explanation", ""));
        aiPlan.setVersion(1);
        aiPlan.setConverted(0);
        aiPlan.setStatus("draft");
        aiPlan.setCreatedAt(LocalDateTime.now());
        aiPlan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(aiPlan);

        // 构建响应
        GeneratePlanResponse response = new GeneratePlanResponse();
        response.setAiPlanId(aiPlan.getId());
        response.setSplitType(aiPlan.getSplitType());
        response.setExplanation(aiPlan.getExplanation());
        response.setWeeklyPlan(parseWeeklyPlan(planData));
        response.setDisclaimer("本计划由AI生成，仅供参考。请根据自身情况调整，如有不适请停止训练并咨询专业人士。");

        return response;
    }

    /**
     * 构建计划生成提示词
     */
    private String buildPlanPrompt(GeneratePlanRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为用户生成一份个性化训练计划，要求如下：\n\n");

        // 训练目标
        Map<String, String> goalMap = Map.of(
                "lose_fat", "减脂",
                "build_muscle", "增肌",
                "strength", "力量提升",
                "endurance", "耐力提升",
                "flexibility", "柔韧性",
                "body_recomp", "身体重塑",
                "general_fitness", "综合体能"
        );
        sb.append("【训练目标】").append(goalMap.getOrDefault(request.getGoal(), request.getGoal())).append("\n");

        // 每周天数
        sb.append("【每周训练天数】").append(request.getDaysPerWeek()).append("天\n");

        // 可用器械
        if (request.getAvailableEquipment() != null && !request.getAvailableEquipment().isEmpty()) {
            Map<String, String> equipMap = Map.of(
                    "none", "无器械(自重)",
                    "dumbbell", "哑铃",
                    "barbell", "杠铃",
                    "band", "弹力带",
                    "kettlebell", "壶铃",
                    "cable", "绳索器械",
                    "machine", "固定器械",
                    "pullup_bar", "引体向上杆",
                    "bench", "训练凳",
                    "full_gym", "全套健身房"
            );
            String equipStr = request.getAvailableEquipment().stream()
                    .map(e -> equipMap.getOrDefault(e, e))
                    .reduce((a, b) -> a + "、" + b)
                    .orElse("无");
            sb.append("【可用器械】").append(equipStr).append("\n");
        }

        // 体测数据
        if (request.getBodyMetrics() != null && !request.getBodyMetrics().isEmpty()) {
            sb.append("【体测数据】");
            request.getBodyMetrics().forEach((k, v) -> {
                Map<String, String> metricMap = Map.of(
                        "level", "训练水平", "weight", "体重(kg)",
                        "bodyFat", "体脂率(%)", "height", "身高(cm)",
                        "age", "年龄", "weeks", "计划周数"
                );
                sb.append(metricMap.getOrDefault(k, k)).append(": ").append(v).append("; ");
            });
            sb.append("\n");
        }

        // 额外说明
        if (request.getAdditionalNotes() != null && !request.getAdditionalNotes().isEmpty()) {
            sb.append("【额外说明】").append(request.getAdditionalNotes()).append("\n");
        }

        sb.append("\n请严格按照JSON格式返回训练计划。");
        return sb.toString();
    }

    /**
     * 从AI响应中提取JSON
     */
    private String extractJson(String response) {
        // 尝试提取 ```json ... ``` 中的内容
        int jsonStart = response.indexOf("```json");
        if (jsonStart >= 0) {
            jsonStart = response.indexOf("\n", jsonStart) + 1;
            int jsonEnd = response.indexOf("```", jsonStart);
            if (jsonEnd > jsonStart) {
                return response.substring(jsonStart, jsonEnd).trim();
            }
        }
        // 尝试提取 ``` ... ``` 中的内容
        jsonStart = response.indexOf("```");
        if (jsonStart >= 0) {
            jsonStart = response.indexOf("\n", jsonStart) + 1;
            int jsonEnd = response.indexOf("```", jsonStart);
            if (jsonEnd > jsonStart) {
                return response.substring(jsonStart, jsonEnd).trim();
            }
        }
        // 尝试提取 { ... } 花括号内容
        jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1).trim();
        }
        return response.trim();
    }

    /**
     * 解析周计划
     */
    @SuppressWarnings("unchecked")
    private List<WeeklyPlanDay> parseWeeklyPlan(Map<String, Object> planData) {
        List<WeeklyPlanDay> result = new ArrayList<>();
        Object weeklyPlanObj = planData.get("weeklyPlan");
        if (weeklyPlanObj == null) {
            weeklyPlanObj = planData.get("weekly_plan");
        }
        if (weeklyPlanObj instanceof List) {
            List<Map<String, Object>> days = (List<Map<String, Object>>) weeklyPlanObj;
            for (Map<String, Object> dayData : days) {
                WeeklyPlanDay day = new WeeklyPlanDay();
                day.setDayOfWeek(dayData.get("dayOfWeek") != null
                        ? ((Number) dayData.get("dayOfWeek")).intValue() : 1);
                day.setDayLabel((String) dayData.getOrDefault("dayLabel", "训练日"));

                List<PlanExercise> exercises = new ArrayList<>();
                Object exercisesObj = dayData.get("exercises");
                if (exercisesObj instanceof List) {
                    for (Map<String, Object> exData : (List<Map<String, Object>>) exercisesObj) {
                        PlanExercise exercise = new PlanExercise();
                        exercise.setExerciseId(exData.get("exerciseId") != null
                                ? ((Number) exData.get("exerciseId")).longValue() : null);
                        exercise.setExerciseName((String) exData.getOrDefault("exerciseName", ""));
                        exercise.setSets(exData.get("sets") != null
                                ? ((Number) exData.get("sets")).intValue() : 3);
                        exercise.setReps((String) exData.getOrDefault("reps", "10"));
                        exercise.setRestSeconds(exData.get("restSeconds") != null
                                ? ((Number) exData.get("restSeconds")).intValue() : 60);
                        exercises.add(exercise);
                    }
                }
                day.setExercises(exercises);
                result.add(day);
            }
        }
        return result;
    }

    /**
     * 备用方案：AI调用失败时生成基础计划
     */
    private GeneratePlanResponse generateFallbackPlan(Long userId, GeneratePlanRequest request) {
        AiPlan aiPlan = new AiPlan();
        aiPlan.setUserId(userId);
        aiPlan.setPrompt("fallback plan for: " + request.getGoal());
        aiPlan.setGoal(request.getGoal());
        aiPlan.setAvailableEquipment(request.getAvailableEquipment() != null
                ? String.join(",", request.getAvailableEquipment()) : null);
        aiPlan.setDaysPerWeek(request.getDaysPerWeek());

        String splitType = determineSplitType(request.getDaysPerWeek());
        aiPlan.setSplitType(splitType);
        aiPlan.setExplanation(buildFallbackExplanation(request));
        aiPlan.setVersion(1);
        aiPlan.setConverted(0);
        aiPlan.setStatus("draft");
        aiPlan.setCreatedAt(LocalDateTime.now());
        aiPlan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(aiPlan);

        GeneratePlanResponse response = new GeneratePlanResponse();
        response.setAiPlanId(aiPlan.getId());
        response.setSplitType(aiPlan.getSplitType());
        response.setExplanation(aiPlan.getExplanation());
        response.setWeeklyPlan(buildFallbackWeeklyPlan(request));
        response.setDisclaimer("AI服务暂时不可用，已生成基础计划。建议稍后重新生成个性化计划。");

        return response;
    }

    private String determineSplitType(Integer daysPerWeek) {
        if (daysPerWeek <= 2) return "full_body";
        if (daysPerWeek <= 3) return "full_body";
        if (daysPerWeek <= 4) return "upper_lower";
        return "push_pull_legs";
    }

    private String buildFallbackExplanation(GeneratePlanRequest request) {
        Map<String, String> goalMap = Map.of(
                "lose_fat", "减脂", "build_muscle", "增肌",
                "strength", "力量提升", "endurance", "耐力提升",
                "general_fitness", "综合体能"
        );
        String goalText = goalMap.getOrDefault(request.getGoal(), "综合训练");
        String splitText = Map.of("full_body", "全身训练", "upper_lower", "上下肢分化", "push_pull_legs", "推拉腿分化")
                .getOrDefault(determineSplitType(request.getDaysPerWeek()), "全身训练");
        return String.format("根据您的%s目标和每周%d天的训练安排，推荐%s方案。每个训练日包含复合动作为主，逐步增加训练强度。",
                goalText, request.getDaysPerWeek(), splitText);
    }

    private List<WeeklyPlanDay> buildFallbackWeeklyPlan(GeneratePlanRequest request) {
        List<WeeklyPlanDay> days = new ArrayList<>();
        String[] dayLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String splitType = determineSplitType(request.getDaysPerWeek());

        // 根据分化类型生成基础训练日
        Map<String, List<String>> exerciseTemplates = Map.of(
                "full_body", List.of("深蹲", "俯卧撑", "哑铃划船", "平板支撑", "弓步蹲"),
                "upper_lower", List.of("卧推", "引体向上", "肩推", "杠铃划船", "弯举"),
                "push_pull_legs", List.of("卧推", "深蹲", "硬拉", "肩推", "划船")
        );
        List<String> exercises = exerciseTemplates.getOrDefault(splitType, exerciseTemplates.get("full_body"));

        int dayCount = 0;
        for (int i = 0; i < 7 && dayCount < request.getDaysPerWeek(); i++) {
            if (shouldTrainOnDay(i, request.getDaysPerWeek())) {
                WeeklyPlanDay day = new WeeklyPlanDay();
                day.setDayOfWeek(i + 1);
                day.setDayLabel(dayLabels[i]);

                List<PlanExercise> planExercises = new ArrayList<>();
                for (String exName : exercises) {
                    PlanExercise pe = new PlanExercise();
                    pe.setExerciseName(exName);
                    pe.setSets(3);
                    pe.setReps("10-12");
                    pe.setRestSeconds(60);
                    planExercises.add(pe);
                }
                day.setExercises(planExercises);
                days.add(day);
                dayCount++;
            }
        }
        return days;
    }

    private boolean shouldTrainOnDay(int dayIndex, int daysPerWeek) {
        return switch (daysPerWeek) {
            case 2 -> dayIndex == 1 || dayIndex == 4; // 周二、周五
            case 3 -> dayIndex == 0 || dayIndex == 2 || dayIndex == 4; // 周一、周三、周五
            case 4 -> dayIndex == 0 || dayIndex == 1 || dayIndex == 3 || dayIndex == 4; // 周一至周五选4天
            case 5 -> dayIndex != 5 && dayIndex != 6; // 周一至周五
            case 6 -> dayIndex != 5; // 周一至周六
            default -> dayIndex == 0 || dayIndex == 2 || dayIndex == 4;
        };
    }

    /** 计划生成系统提示词 */
    private static final String PLAN_SYSTEM_PROMPT = """
            你是一个专业的AI健身计划生成器。用户会提供训练目标、可用器械、每周训练天数等信息，你需要生成一份个性化的训练计划。
            
            你必须严格按照以下JSON格式返回，不要包含任何其他文字说明：
            
            ```json
            {
              "splitType": "full_body | upper_lower | push_pull_legs | body_part",
              "explanation": "计划说明：为什么推荐这种分化方案，训练逻辑和注意事项",
              "weeklyPlan": [
                {
                  "dayOfWeek": 1,
                  "dayLabel": "周一 - 全身A",
                  "exercises": [
                    {
                      "exerciseName": "杠铃深蹲",
                      "sets": 4,
                      "reps": "8-10",
                      "restSeconds": 90
                    }
                  ]
                }
              ]
            }
            ```
            
            规则：
            1. splitType根据天数决定：2-3天用full_body，4天用upper_lower，5+天用push_pull_legs
            2. 每个训练日4-6个动作，复合动作优先
            3. sets通常3-4组，reps根据目标调整（增肌8-12，力量4-6，耐力15-20）
            4. restSeconds：复合动作90-120秒，孤立动作60-90秒
            5. 只安排训练日，休息日不要放入weeklyPlan
            6. exerciseName必须是常见健身动作的中文名称
            7. explanation要详细说明训练逻辑、进阶策略和注意事项
            """;

    /**
     * 确认AI计划 - 转换为正式训练计划
     */
    @Transactional
    public ConfirmPlanResponse confirmPlan(Long id) {
        Long userId = getCurrentUserId();

        AiPlan aiPlan = planMapper.selectById(id);
        if (aiPlan == null || !aiPlan.getUserId().equals(userId)) {
            throw new BizException("计划不存在");
        }
        if ("confirmed".equals(aiPlan.getStatus())) {
            throw new BizException("计划已确认");
        }

        // 1. 创建 workout_plan 记录
        WorkoutPlan workoutPlan = new WorkoutPlan();
        workoutPlan.setName(buildPlanName(aiPlan));
        workoutPlan.setDescription(aiPlan.getExplanation());
        workoutPlan.setDifficultyLevel(inferDifficulty(aiPlan));
        workoutPlan.setFitnessGoal(aiPlan.getGoal());
        workoutPlan.setDaysPerWeek(aiPlan.getDaysPerWeek());
        workoutPlan.setDurationWeeks(extractDurationWeeks(aiPlan));
        workoutPlan.setAvgDurationMin(45);
        workoutPlan.setIsSystem(0);
        workoutPlan.setCreatedBy(userId);
        workoutPlan.setAiGenerated(1);
        workoutPlan.setAiGenerationParams(aiPlan.getGenerationParams());
        workoutPlan.setStatus(1);
        workoutPlan.setCreatedAt(LocalDateTime.now());
        workoutPlan.setUpdatedAt(LocalDateTime.now());
        workoutPlanMapper.insert(workoutPlan);

        Long workoutPlanId = workoutPlan.getId();

        // 2. 解析AI响应中的周计划，创建 plan_day 和 plan_day_exercise
        List<WeeklyPlanDay> weeklyPlan = parseResponseToWeeklyPlan(aiPlan);
        int weekNumber = 1;
        int daySortOrder = 0;
        for (WeeklyPlanDay dayData : weeklyPlan) {
            PlanDay planDay = new PlanDay();
            planDay.setPlanId(workoutPlanId);
            planDay.setWeekNumber(weekNumber);
            planDay.setDayOfWeek(dayData.getDayOfWeek());
            planDay.setDayLabel(dayData.getDayLabel());
            planDay.setIsRestDay(0);
            planDay.setSortOrder(daySortOrder++);
            planDayMapper.insert(planDay);

            Long planDayId = planDay.getId();

            // 3. 创建 plan_day_exercise 记录
            if (dayData.getExercises() != null) {
                int exerciseSort = 0;
                for (PlanExercise exData : dayData.getExercises()) {
                    PlanDayExercise pde = new PlanDayExercise();
                    pde.setPlanDayId(planDayId);
                    pde.setExerciseId(exData.getExerciseId());
                    pde.setSets(exData.getSets() != null ? exData.getSets() : 3);
                    pde.setReps(exData.getReps() != null ? exData.getReps() : "10");
                    pde.setRestSeconds(exData.getRestSeconds() != null ? exData.getRestSeconds() : 60);
                    pde.setSort(exerciseSort++);
                    planDayExerciseMapper.insert(pde);
                }
            }
        }

        // 如果没有解析到周计划数据，生成默认训练日
        if (weeklyPlan.isEmpty()) {
            createDefaultPlanDays(workoutPlanId, aiPlan.getDaysPerWeek());
        }

        // 更新AI计划状态
        aiPlan.setStatus("confirmed");
        aiPlan.setConverted(1);
        aiPlan.setConvertedPlanId(workoutPlanId);
        aiPlan.setWorkoutPlanId(workoutPlanId);
        aiPlan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(aiPlan);

        ConfirmPlanResponse response = new ConfirmPlanResponse();
        response.setAiPlanId(aiPlan.getId());
        response.setWorkoutPlanId(workoutPlanId);
        response.setStatus("confirmed");

        return response;
    }

    /**
     * 构建计划名称
     */
    private String buildPlanName(AiPlan aiPlan) {
        Map<String, String> goalMap = Map.of(
                "lose_fat", "减脂", "build_muscle", "增肌",
                "strength", "力量提升", "endurance", "耐力提升",
                "flexibility", "柔韧性", "body_recomp", "身体重塑",
                "general_fitness", "综合体能"
        );
        String goalText = goalMap.getOrDefault(aiPlan.getGoal(), "健身");
        return goalText + "计划 - " + aiPlan.getDaysPerWeek() + "天/周";
    }

    /**
     * 推断难度等级
     */
    private String inferDifficulty(AiPlan aiPlan) {
        if (aiPlan.getGenerationParams() != null) {
            try {
                Map<String, Object> params = objectMapper.readValue(
                        aiPlan.getGenerationParams(), new TypeReference<Map<String, Object>>() {});
                Object level = params.get("level");
                if (level != null) {
                    String levelStr = level.toString().toLowerCase();
                    if (levelStr.contains("beginner") || levelStr.contains("新手")) return "beginner";
                    if (levelStr.contains("intermediate") || levelStr.contains("中级")) return "intermediate";
                    if (levelStr.contains("advanced") || levelStr.contains("高级")) return "advanced";
                }
            } catch (Exception ignored) {}
        }
        return "beginner";
    }

    /**
     * 提取计划周数
     */
    private Integer extractDurationWeeks(AiPlan aiPlan) {
        if (aiPlan.getGenerationParams() != null) {
            try {
                Map<String, Object> params = objectMapper.readValue(
                        aiPlan.getGenerationParams(), new TypeReference<Map<String, Object>>() {});
                Object weeks = params.get("weeks");
                if (weeks instanceof Number) {
                    return ((Number) weeks).intValue();
                }
            } catch (Exception ignored) {}
        }
        return 4;
    }

    /**
     * 从AI响应中解析周计划
     */
    @SuppressWarnings("unchecked")
    private List<WeeklyPlanDay> parseResponseToWeeklyPlan(AiPlan aiPlan) {
        if (aiPlan.getResponse() == null || aiPlan.getResponse().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            String jsonStr = extractJson(aiPlan.getResponse());
            Map<String, Object> planData = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            return parseWeeklyPlan(planData);
        } catch (Exception e) {
            log.warn("解析AI计划响应失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成默认训练日（AI响应解析失败时使用）
     */
    private void createDefaultPlanDays(Long workoutPlanId, Integer daysPerWeek) {
        String[] dayLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int sortOrder = 0;
        for (int i = 0; i < 7 && sortOrder < daysPerWeek; i++) {
            if (shouldTrainOnDay(i, daysPerWeek)) {
                PlanDay planDay = new PlanDay();
                planDay.setPlanId(workoutPlanId);
                planDay.setWeekNumber(1);
                planDay.setDayOfWeek(i + 1);
                planDay.setDayLabel(dayLabels[i]);
                planDay.setIsRestDay(0);
                planDay.setSortOrder(sortOrder++);
                planDayMapper.insert(planDay);
            }
        }
    }

    /**
     * 获取AI计划详情
     */
    public AiPlan getPlanDetail(Long id) {
        Long userId = getCurrentUserId();

        AiPlan aiPlan = planMapper.selectById(id);
        if (aiPlan == null || !aiPlan.getUserId().equals(userId)) {
            throw new BizException("计划不存在");
        }

        return aiPlan;
    }

    /**
     * 获取AI计划列表
     */
    public PageResult<AiPlan> getPlanList(Integer pageNum, Integer pageSize, String status) {
        Long userId = getCurrentUserId();

        Page<AiPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiPlan::getUserId, userId);
        if (status != null) {
            wrapper.eq(AiPlan::getStatus, status);
        }
        wrapper.orderByDesc(AiPlan::getCreatedAt);
        Page<AiPlan> result = planMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 创建新会话
     */
    private AiChatSession createNewSession(Long userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionType("fitness");
        session.setMessageCount(0);
        session.setStatus(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
