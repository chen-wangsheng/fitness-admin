# 运动健身助手 — 管理后台 Java 后端服务产品需求文档 (PRD)

> **版本**: v2.0  
> **日期**: 2026-05-28  
> **关联项目**: 运动健身助手小程序 v1.1、管理后台 Web 端 v2.0  
> **技术栈**: Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL 8.0 + Redis 7 + Milvus 2.x + Docker  
> **架构风格**: Spring Boot 单体多模块 + Docker 部署（无 Spring Cloud）  
> **变更记录**: v2.0 — 首次独立后端 PRD，按 PRD 功能模块拆分 Maven 子模块，完整 Docker 部署方案

---

## 一、项目概述

### 1.1 产品定位

运动健身助手小程序管理后台的后端服务，为前端 Web 管理后台提供 RESTful API，同时与小程序云函数共享 MySQL 数据库。核心职责包括：用户认证与权限管理、内容管理 CRUD、数据统计聚合、**AI 知识库管理、RAG Pipeline、LLM 调用、安全过滤引擎**等。

### 1.2 技术栈总览

| 层级 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| 语言 | Java | 17 (LTS) | Eclipse Temurin |
| 框架 | Spring Boot | 3.2.5 | Web / WebSocket / Actuator |
| ORM | MyBatis-Plus | 3.5.6 | 代码生成、分页、乐观锁、自动填充 |
| 认证 | Sa-Token | 1.38.0 | JWT + RBAC，轻量级 |
| 接口文档 | Knife4j (Swagger) | 4.4.0 | OpenAPI 3.0 |
| 数据库 | MySQL | 8.0 | CloudBase 云数据库 |
| 缓存 | Redis | 7 | 会话/缓存/限流 |
| 向量数据库 | Milvus | 2.4 | 知识库向量索引 |
| 对象存储 | 腾讯云 COS | — | 媒体文件存储 |
| AI-LLM | OpenAI Java SDK | 0.21.0 | 兼容 OpenAI/DeepSeek/Qwen API |
| 工具库 | Hutool | 5.8.26 | 常用工具集 |
| 对象映射 | MapStruct | 1.5.5 | DTO ↔ Entity 转换 |
| Excel | EasyExcel | 3.3.4 | 导入导出 |
| 容器化 | Docker | 24+ | 多阶段构建 |

### 1.3 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        用户访问层                             │
│            浏览器 → CloudBase 静态托管 (Vue SPA)              │
│                     ↕ HTTPS                                  │
├─────────────────────────────────────────────────────────────┤
│                     Nginx 反向代理                            │
│           (SSL 终止 / 负载均衡 / 限流 / 静态资源)              │
├──────────────────────┬──────────────────────────────────────┤
│   /api/admin/*       │         /ws/* (WebSocket)             │
│   → 后端服务 :8080    │         → 后端服务 :8080               │
├──────────────────────┴──────────────────────────────────────┤
│              fitness-admin (Spring Boot 单体应用)             │
│   ┌──────────────────────────────────────────────────────┐  │
│   │  common │ user │ content │ workout │ achievement      │  │
│   │  community │ ai │ system │ admin-app (启动入口)        │  │
│   └──────────────────────────────────────────────────────┘  │
├──────────┬──────────┬──────────────┬────────────────────────┤
│ MySQL 8.0│ Redis 7  │  Milvus 2.x  │  COS 对象存储           │
└──────────┴──────────┴──────────────┴────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   外部 LLM API     │
                    │  (OpenAI/DeepSeek/ │
                    │   Qwen/自建模型)    │
                    └───────────────────┘
```

---

## 二、整体架构

### 2.1 系统分层

```
┌─────────────────────────────────────────────────────────────┐
│  Controller 层 (REST API)                                    │
│  · 参数校验 (@Valid) · 响应封装 (R<T>) · 权限注解 (@SaCheck) │
├─────────────────────────────────────────────────────────────┤
│  Service 层 (业务逻辑)                                       │
│  · 事务管理 (@Transactional) · 业务校验 · 跨模块调用          │
├─────────────────────────────────────────────────────────────┤
│  Mapper 层 (数据访问)                                        │
│  · MyBatis-Plus BaseMapper · 自定义 XML SQL · 分页查询       │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure 层 (基础设施)                                │
│  · Redis · Milvus · COS · LLM API · WebSocket               │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 模块依赖关系

```
fitness-common (最底层, 无业务依赖)
    ↑
    ├── fitness-user            ← 依赖 common
    ├── fitness-content         ← 依赖 common
    ├── fitness-achievement     ← 依赖 common
    ├── fitness-community       ← 依赖 common
    │
    ├── fitness-workout         ← 依赖 common + user + content
    ├── fitness-ai              ← 依赖 common + user + content
    │
    └── fitness-system          ← 依赖 common + user + workout + achievement + ai

fitness-admin-app (启动入口, 聚合所有模块)
```

**跨模块调用规则**：
- 模块间通过 **接口 (Interface)** 定义依赖，**实现 (Impl)** 在各自模块内
- 例如：fitness-workout 需查用户信息 → 依赖 fitness-user 暴露的 `UserService.getUserById()`
- 严格单向依赖，禁止循环依赖

### 2.3 数据流向

```
小程序云函数 ──写入──→ MySQL 共享表 ←──读写── 管理后台后端
                              │
                              ├──→ Redis (会话/缓存/限流)
                              ├──→ Milvus (向量索引)
                              ├──→ COS (媒体文件)
                              └──→ LLM API (AI 对话/计划生成)
```

---

## 三、Maven 多模块设计

### 3.1 模块总览

```
fitness-admin/                              # 父 POM (聚合)
├── fitness-common/                         # 公共基础模块
├── fitness-user/                           # 用户与认证模块
├── fitness-content/                        # 内容管理模块 (计划/动作)
├── fitness-workout/                        # 训练与身体数据模块
├── fitness-achievement/                    # 成就与打卡模块
├── fitness-community/                      # 社区管理模块
├── fitness-ai/                             # AI 管理模块
├── fitness-system/                         # 系统设置与权限模块
├── fitness-admin-app/                      # 启动入口模块
└── docker/
    ├── Dockerfile
    ├── docker-compose.yml
    └── nginx/nginx.conf
```

### 3.2 模块职责与 PRD 对应

| Maven 模块 | 对应前端 PRD 章节 | 职责范围 | 核心表 |
|-----------|-----------------|---------|--------|
| **fitness-common** | — | 公共工具、统一响应、异常处理、全局配置、公共枚举 | — |
| **fitness-user** | 3.2 用户管理 | 用户 CRUD、用户标签、用户导出、AI 画像查看 | user, user_tag, user_tag_relation |
| **fitness-content** | 3.3 训练计划 + 3.4 动作库 | 计划 CRUD、动作库 CRUD、分类管理、部位管理 | workout_plan, plan_day, plan_exercise, exercise, exercise_category, body_part |
| **fitness-workout** | 3.5 训练数据 + 3.6 身体数据 | 训练记录查询、数据统计、身体数据管理 | workout_log, workout_log_exercise, workout_log_set, body_metric |
| **fitness-achievement** | 3.7 成就与打卡 | 成就 CRUD、打卡统计、排行榜 | achievement, checkin, user_achievement |
| **fitness-community** | 3.8 社区管理 | 动态审核、评论管理、举报处理、敏感词 | post, comment, sensitive_word |
| **fitness-ai** | 3.9~3.13 AI 管理 | 知识库、对话监控、AI 计划、安全规则、Prompt、AI 分析、RAG Pipeline | ai_chat_*, ai_plan, knowledge_base, ai_safety_*, ai_prompt_*, ai_usage_daily, vector_index |
| **fitness-system** | 3.1 看板 + 3.14 系统 + 3.15 权限 | Dashboard 统计、系统配置、公告、操作日志、AI 模型配置、角色、管理员 | sys_config, announcement, admin_*, ai_adjustment_config |
| **fitness-admin-app** | — | 启动入口、Web 配置、WebSocket、Sa-Token 配置 | — |

### 3.3 各模块详细结构

#### fitness-common（公共基础模块）

```
fitness-common/
├── src/main/java/com/fitness/admin/common/
│   ├── config/
│   │   ├── JacksonConfig.java            # JSON 序列化配置
│   │   ├── MyBatisPlusConfig.java        # 分页插件/乐观锁/自动填充
│   │   ├── CorsConfig.java               # 跨域配置
│   │   └── RedisConfig.java              # Redis 序列化配置
│   ├── constant/
│   │   ├── CommonConstant.java
│   │   ├── RedisKeyConstant.java
│   │   └── CacheConstant.java
│   ├── enums/
│   │   ├── StatusEnum.java
│   │   ├── GenderEnum.java
│   │   ├── FitnessGoalEnum.java          # lose_fat/gain_muscle/keep_fit/improve_endurance
│   │   ├── FitnessLevelEnum.java         # beginner/intermediate/advanced
│   │   ├── ExerciseTypeEnum.java         # strength/cardio/flexibility/balance
│   │   ├── EquipmentEnum.java            # none/dumbbell/barbell/machine/cable/band
│   │   ├── DifficultyEnum.java
│   │   ├── VectorStatusEnum.java         # pending/indexing/indexed/failed
│   │   └── ResultCodeEnum.java           # 业务状态码
│   ├── exception/
│   │   ├── BizException.java             # 业务异常
│   │   ├── GlobalExceptionHandler.java   # @RestControllerAdvice 全局异常拦截
│   │   └── ErrorCode.java               # 错误码定义
│   ├── result/
│   │   ├── R.java                        # 统一响应体 R<T>
│   │   └── PageResult.java              # 分页响应体
│   ├── utils/
│   │   ├── SecurityUtil.java             # 获取当前登录用户 (Sa-Token)
│   │   ├── IpUtil.java
│   │   ├── ExcelUtil.java               # EasyExcel 封装
│   │   └── DateUtil.java
│   ├── annotation/
│   │   ├── Log.java                      # 操作日志注解
│   │   └── RateLimit.java               # 限流注解
│   ├── aspect/
│   │   ├── LogAspect.java                # 操作日志切面 (AOP)
│   │   └── RateLimitAspect.java          # 限流切面 (Redis 滑动窗口)
│   └── base/
│       ├── BaseEntity.java               # 实体基类 (id/createdAt/updatedAt)
│       └── BaseController.java
└── pom.xml
```

#### fitness-user（用户与认证模块）

```
fitness-user/
├── src/main/java/com/fitness/admin/user/
│   ├── controller/
│   │   ├── AuthController.java           # POST /auth/login, /logout, /refresh, /profile, /password
│   │   └── UserController.java           # 用户 CRUD + 导出 + AI画像 + AI对话历史 + 批量操作
│   ├── service/
│   │   ├── AuthService.java              # 登录/登出/Token 刷新 (Sa-Token)
│   │   ├── UserService.java              # 用户业务逻辑
│   │   └── UserTagService.java           # 用户标签管理
│   ├── mapper/
│   │   ├── UserMapper.java               # MyBatis-Plus BaseMapper<User>
│   │   └── UserTagMapper.java
│   ├── entity/
│   │   ├── AdminUser.java                # 管理员独立表实体
│   │   ├── User.java                     # 小程序用户表实体
│   │   ├── UserTag.java
│   │   └── UserTagRelation.java
│   ├── dto/
│   │   ├── LoginRequest.java             # username + password
│   │   ├── LoginResponse.java            # token + userInfo
│   │   ├── UserQueryDTO.java             # 分页/筛选/搜索参数
│   │   ├── UserUpdateDTO.java
│   │   └── BatchStatusDTO.java           # user_ids + status + reason
│   ├── vo/
│   │   ├── UserVO.java
│   │   ├── UserDetailVO.java             # 含训练统计/成就/AI画像
│   │   └── UserProfileVO.java
│   └── config/
│       └── SaTokenConfig.java            # Sa-Token 认证配置
└── pom.xml
```

#### fitness-content（内容管理模块）

```
fitness-content/
├── src/main/java/com/fitness/admin/content/
│   ├── controller/
│   │   ├── PlanController.java           # 训练计划 CRUD + 上下架 + 复制 + AI筛选
│   │   ├── ExerciseController.java       # 动作库 CRUD + 媒体上传
│   │   ├── CategoryController.java       # 动作分类 CRUD
│   │   └── BodyPartController.java       # 身体部位 CRUD
│   ├── service/
│   │   ├── PlanService.java
│   │   ├── PlanDayService.java
│   │   ├── ExerciseService.java
│   │   ├── CategoryService.java
│   │   └── BodyPartService.java
│   ├── mapper/
│   │   ├── PlanMapper.java
│   │   ├── ExerciseMapper.java
│   │   ├── CategoryMapper.java
│   │   └── BodyPartMapper.java
│   ├── entity/
│   │   ├── WorkoutPlan.java
│   │   ├── PlanDay.java
│   │   ├── PlanDayExercise.java
│   │   ├── Exercise.java
│   │   ├── ExerciseCategory.java
│   │   ├── BodyPart.java
│   │   └── ExerciseBodyPart.java
│   ├── dto/
│   │   ├── PlanCreateDTO.java
│   │   ├── PlanQueryDTO.java
│   │   ├── ExerciseCreateDTO.java
│   │   └── ExerciseQueryDTO.java
│   └── vo/
│       ├── PlanVO.java
│       ├── PlanDetailVO.java             # 含每日安排+动作列表
│       └── ExerciseVO.java
└── pom.xml
```

#### fitness-workout（训练与身体数据模块）

```
fitness-workout/
├── src/main/java/com/fitness/admin/workout/
│   ├── controller/
│   │   ├── WorkoutRecordController.java  # 训练记录查询 + 导出
│   │   ├── WorkoutAnalyticsController.java # 训练数据统计看板
│   │   └── BodyMetricController.java     # 身体数据管理
│   ├── service/
│   │   ├── WorkoutRecordService.java
│   │   ├── WorkoutAnalyticsService.java  # 统计聚合 (日/周/月趋势/高峰时段/热度)
│   │   └── BodyMetricService.java
│   ├── mapper/
│   │   ├── WorkoutLogMapper.java
│   │   └── BodyMetricMapper.java
│   ├── entity/
│   │   ├── WorkoutLog.java
│   │   └── BodyMetric.java
│   ├── dto/
│   │   ├── WorkoutQueryDTO.java
│   │   └── BodyMetricQueryDTO.java
│   └── vo/
│       ├── WorkoutRecordVO.java
│       ├── WorkoutOverviewVO.java
│       ├── WorkoutTrendVO.java
│       ├── PeakHourVO.java
│       ├── ExercisePopularityVO.java
│       └── BodyMetricVO.java
└── pom.xml
```

#### fitness-achievement（成就与打卡模块）

```
fitness-achievement/
├── src/main/java/com/fitness/admin/achievement/
│   ├── controller/
│   │   ├── AchievementController.java
│   │   └── CheckinController.java
│   ├── service/
│   │   ├── AchievementService.java
│   │   └── CheckinService.java
│   ├── mapper/
│   │   ├── AchievementMapper.java
│   │   └── CheckinMapper.java
│   ├── entity/
│   │   ├── Achievement.java
│   │   └── Checkin.java
│   ├── dto/
│   │   └── AchievementCreateDTO.java
│   └── vo/
│       ├── AchievementVO.java
│       └── CheckinStatsVO.java
└── pom.xml
```

#### fitness-community（社区管理模块）

```
fitness-community/
├── src/main/java/com/fitness/admin/community/
│   ├── controller/
│   │   ├── CommunityPostController.java
│   │   ├── CommunityCommentController.java
│   │   ├── ReportController.java
│   │   └── SensitiveWordController.java
│   ├── service/
│   │   ├── CommunityPostService.java
│   │   ├── CommunityCommentService.java
│   │   ├── ReportService.java
│   │   └── SensitiveWordService.java
│   ├── mapper/
│   │   ├── CommunityPostMapper.java
│   │   ├── CommunityCommentMapper.java
│   │   ├── ReportMapper.java
│   │   └── SensitiveWordMapper.java
│   ├── entity/
│   │   ├── CommunityPost.java
│   │   ├── CommunityComment.java
│   │   ├── Report.java
│   │   └── SensitiveWord.java
│   ├── dto/
│   │   ├── PostAuditDTO.java
│   │   └── ReportHandleDTO.java
│   └── vo/
│       ├── PostVO.java
│       ├── CommentVO.java
│       └── ReportVO.java
└── pom.xml
```

#### fitness-ai（AI 管理模块）

```
fitness-ai/
├── src/main/java/com/fitness/admin/ai/
│   ├── controller/
│   │   ├── AiKnowledgeController.java    # 知识库 CRUD + RAG 测试
│   │   ├── AiChatMonitorController.java  # 对话监控
│   │   ├── AiPlanController.java         # AI 计划管理
│   │   ├── AiSafetyController.java       # 安全规则 + Prompt
│   │   └── AiAnalyticsController.java    # AI 数据分析
│   ├── service/
│   │   ├── AiKnowledgeService.java       # 知识库 CRUD + 向量化触发
│   │   ├── AiChatMonitorService.java     # 对话查询 + 问题标记 + 反哺知识库
│   │   ├── AiPlanService.java            # AI 计划审核/统计/转系统计划
│   │   ├── AiSafetyRuleService.java      # 安全规则 CRUD
│   │   ├── AiPromptTemplateService.java  # Prompt 模板版本管理
│   │   ├── AiAnalyticsService.java       # AI 统计聚合
│   │   └── VectorIndexService.java       # 向量索引管理 (创建/更新/删除/重建)
│   ├── rag/                              # RAG Pipeline 核心
│   │   ├── RagService.java               # RAG 检索主流程 (编排)
│   │   ├── QueryRewriter.java            # Step 1: Query 改写
│   │   ├── VectorRetriever.java          # Step 2: 向量检索 (Milvus)
│   │   ├── KeywordRetriever.java         # Step 3: 关键词检索 (MySQL FULLTEXT)
│   │   ├── HybridRanker.java             # Step 4: RRF 融合排序
│   │   ├── ContextBuilder.java           # Step 5: 上下文组装
│   │   └── PromptAssembler.java          # Step 6: Prompt 拼装
│   ├── llm/                              # LLM 调用层
│   │   ├── LlmClient.java               # 统一 LLM 调用客户端 (SSE 流式)
│   │   ├── EmbeddingClient.java          # Embedding 向量化
│   │   └── LlmConfig.java              # LLM 动态配置
│   ├── safety/                           # 安全过滤引擎
│   │   ├── SafetyFilterService.java      # 安全过滤主服务 (预检+后检)
│   │   ├── RuleEngine.java              # 规则匹配引擎 (Aho-Corasick + 正则)
│   │   └── ContentSanitizer.java        # 内容清洗
│   ├── websocket/
│   │   └── AiChatWsHandler.java          # AI 对话实时推送
│   ├── mapper/
│   │   ├── AiKnowledgeMapper.java
│   │   ├── AiKnowledgeCategoryMapper.java
│   │   ├── AiChatSessionMapper.java
│   │   ├── AiChatMessageMapper.java
│   │   ├── AiChatIssueMapper.java
│   │   ├── AiPlanMapper.java
│   │   ├── AiPlanAdjustmentMapper.java
│   │   ├── AiSafetyRuleMapper.java
│   │   ├── AiSafetyEventMapper.java
│   │   ├── AiPromptTemplateMapper.java
│   │   └── AiUsageDailyMapper.java
│   ├── entity/
│   │   ├── AiKnowledge.java
│   │   ├── AiKnowledgeCategory.java
│   │   ├── AiChatSession.java
│   │   ├── AiChatMessage.java
│   │   ├── AiChatIssue.java
│   │   ├── AiPlan.java
│   │   ├── AiPlanAdjustment.java
│   │   ├── AiSafetyRule.java
│   │   ├── AiSafetyEvent.java
│   │   ├── AiPromptTemplate.java
│   │   └── AiUsageDaily.java
│   ├── dto/
│   │   ├── KnowledgeCreateDTO.java
│   │   ├── KnowledgeQueryDTO.java
│   │   ├── RagTestRequest.java
│   │   ├── ChatSessionQueryDTO.java
│   │   ├── IssueFlagDTO.java
│   │   ├── SafetyRuleCreateDTO.java
│   │   ├── PromptTemplateCreateDTO.java
│   │   └── AiAnalyticsQueryDTO.java
│   ├── vo/
│   │   ├── KnowledgeVO.java
│   │   ├── KnowledgeDetailVO.java
│   │   ├── VectorStatusVO.java
│   │   ├── RagTestResultVO.java
│   │   ├── ChatSessionVO.java
│   │   ├── ChatMessageVO.java
│   │   ├── AiPlanVO.java
│   │   ├── AiPlanDetailVO.java
│   │   ├── SafetyRuleVO.java
│   │   ├── SafetyEventVO.java
│   │   ├── PromptTemplateVO.java
│   │   └── AiOverviewVO.java
│   └── config/
│       ├── MilvusConfig.java             # Milvus 连接配置
│       ├── LlmProperties.java            # LLM 配置属性
│       ├── RagProperties.java            # RAG 参数配置
│       └── AiWebSocketConfig.java        # WebSocket 配置
└── pom.xml
```

#### fitness-system（系统设置与权限模块）

```
fitness-system/
├── src/main/java/com/fitness/admin/system/
│   ├── controller/
│   │   ├── DashboardController.java      # 数据看板 (统计聚合入口)
│   │   ├── SystemConfigController.java   # 系统参数配置
│   │   ├── AiConfigController.java       # AI 模型配置
│   │   ├── AnnouncementController.java   # 公告管理
│   │   ├── OperationLogController.java   # 操作日志查询
│   │   ├── AdminController.java          # 管理员账号管理
│   │   ├── RoleController.java           # 角色管理
│   │   └── LoginLogController.java       # 登录日志
│   ├── service/
│   │   ├── DashboardService.java         # 看板数据聚合
│   │   ├── SystemConfigService.java
│   │   ├── AiConfigService.java          # AI 模型配置管理
│   │   ├── AnnouncementService.java
│   │   ├── OperationLogService.java
│   │   ├── AdminService.java
│   │   ├── RoleService.java
│   │   └── LoginLogService.java
│   ├── mapper/
│   │   ├── SystemConfigMapper.java
│   │   ├── AnnouncementMapper.java
│   │   ├── OperationLogMapper.java
│   │   ├── AdminRoleMapper.java
│   │   ├── AdminUserMapper.java
│   │   └── LoginLogMapper.java
│   ├── entity/
│   │   ├── AdminUser.java
│   │   ├── AdminRole.java
│   │   ├── SystemConfig.java
│   │   ├── Announcement.java
│   │   ├── OperationLog.java
│   │   └── LoginLog.java
│   ├── dto/
│   │   ├── DashboardQueryDTO.java
│   │   ├── ConfigUpdateDTO.java
│   │   ├── AiConfigUpdateDTO.java
│   │   ├── AnnouncementCreateDTO.java
│   │   ├── RoleCreateDTO.java
│   │   └── AdminCreateDTO.java
│   ├── vo/
│   │   ├── DashboardVO.java
│   │   ├── AiConfigVO.java
│   │   ├── AnnouncementVO.java
│   │   ├── OperationLogVO.java
│   │   ├── RoleVO.java
│   │   ├── AdminVO.java
│   │   └── LoginLogVO.java
│   └── config/
│       └── WebMvcConfig.java
└── pom.xml
```

#### fitness-admin-app（启动入口模块）

```
fitness-admin-app/
├── src/main/java/com/fitness/admin/
│   └── FitnessAdminApplication.java      # @SpringBootApplication
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── logback-spring.xml
│   └── db/
│       ├── schema.sql                    # 全量建表语句
│       └── data.sql                      # 初始化数据
└── pom.xml
```

### 3.4 模块依赖声明

```xml
<!-- fitness-admin-app/pom.xml -->
<dependencies>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-common</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-user</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-content</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-workout</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-achievement</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-community</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-ai</artifactId></dependency>
    <dependency><groupId>com.fitness</groupId><artifactId>fitness-system</artifactId></dependency>
</dependencies>
```

### 3.5 父 POM 依赖管理

```xml
<!-- fitness-admin/pom.xml -->
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.5</spring-boot.version>
    <mybatis-plus.version>3.5.6</mybatis-plus.version>
    <sa-token.version>1.38.0</sa-token.version>
    <knife4j.version>4.4.0</knife4j.version>
    <hutool.version>5.8.26</hutool.version>
    <milvus-sdk.version>2.4.1</milvus-sdk.version>
    <openai-java.version>0.21.0</openai-java.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <redisson.version>3.27.2</redisson.version>
    <easyexcel.version>3.3.4</easyexcel.version>
</properties>

<modules>
    <module>fitness-common</module>
    <module>fitness-user</module>
    <module>fitness-content</module>
    <module>fitness-workout</module>
    <module>fitness-achievement</module>
    <module>fitness-community</module>
    <module>fitness-ai</module>
    <module>fitness-system</module>
    <module>fitness-admin-app</module>
</modules>
```

---

## 四、数据库设计

### 4.1 管理员独立表

```sql
-- ═══════════════════════════════════════════════════════════
-- 管理员独立表 — 不侵入小程序 user 表
-- ═══════════════════════════════════════════════════════════

CREATE TABLE `admin_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联小程序 user.id',
  `username` VARCHAR(64) NOT NULL COMMENT '管理员登录用户名',
  `password_hash` VARCHAR(256) NOT NULL COMMENT '密码哈希(bcrypt)',
  `role_id` INT UNSIGNED DEFAULT NULL COMMENT '角色ID(关联 admin_role.id)',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1-正常 2-禁用',
  `last_login_at` DATETIME DEFAULT NULL,
  `last_login_ip` VARCHAR(45) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_role` (`role_id`),
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_admin_role` FOREIGN KEY (`role_id`) REFERENCES `admin_role`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员账号';

CREATE TABLE `admin_role` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(256) DEFAULT NULL,
  `permissions` JSON NOT NULL COMMENT '权限配置JSON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理角色';

CREATE TABLE `admin_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin_user_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID(关联 admin_user.id)',
  `action` VARCHAR(16) NOT NULL COMMENT 'CREATE/UPDATE/DELETE/LOGIN/EXPORT',
  `module` VARCHAR(32) NOT NULL COMMENT '操作模块',
  `target_id` VARCHAR(64) DEFAULT NULL COMMENT '操作对象ID',
  `detail` JSON DEFAULT NULL COMMENT '变更详情',
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin` (`admin_user_id`),
  KEY `idx_module_action` (`module`, `action`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

CREATE TABLE `admin_login_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '管理员ID(关联 admin_user.id)',
  `username` VARCHAR(64) NOT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL COMMENT '0-失败 1-成功',
  `fail_reason` VARCHAR(128) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin` (`admin_user_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';
```

### 4.2 公共基础表

```sql
CREATE TABLE `announcement` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NOT NULL,
  `type` VARCHAR(16) NOT NULL DEFAULT 'info' COMMENT 'info/warning/activity',
  `target` VARCHAR(32) NOT NULL DEFAULT 'all' COMMENT 'all/active_user/new_user',
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `is_popup` TINYINT(1) NOT NULL DEFAULT 0,
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0-草稿 1-已发布',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '关联 admin_user.id',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告管理';

CREATE TABLE `sensitive_word` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `word` VARCHAR(64) NOT NULL,
  `category` VARCHAR(32) DEFAULT 'general',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '1-替换 2-拦截 3-审核',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

CREATE TABLE `user_tag` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL,
  `color` VARCHAR(16) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

CREATE TABLE `user_tag_relation` (
  `user_id` BIGINT UNSIGNED NOT NULL,
  `tag_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`user_id`, `tag_id`),
  CONSTRAINT `fk_utr_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_utr_tag` FOREIGN KEY (`tag_id`) REFERENCES `user_tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签关联';
```

### 4.3 AI 管理相关表

```sql
CREATE TABLE `ai_chat_issue` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '问题消息ID(ai_chat_message.id)',
  `session_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `admin_user_id` BIGINT UNSIGNED NOT NULL COMMENT '标记的管理员(关联 admin_user.id)',
  `issue_type` VARCHAR(32) NOT NULL COMMENT 'hallucination/inaccurate/unsafe/irrelevant',
  `description` TEXT COMMENT '问题描述',
  `correct_answer` TEXT COMMENT '修正后的正确回答',
  `knowledge_added` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已添加到知识库',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`),
  KEY `idx_type` (`issue_type`),
  CONSTRAINT `fk_aci_message` FOREIGN KEY (`message_id`) REFERENCES `ai_chat_message`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI回答问题记录';

CREATE TABLE `ai_safety_rule` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_type` VARCHAR(32) NOT NULL COMMENT 'blocked_topic/sensitive_word/required_disclaimer/forbidden_content',
  `match_mode` VARCHAR(16) NOT NULL DEFAULT 'keyword' COMMENT 'keyword(关键词)/regex(正则)',
  `pattern` TEXT NOT NULL COMMENT '规则模式: keyword=逗号分隔; regex=正则表达式',
  `action` VARCHAR(16) NOT NULL COMMENT 'block/warn/disclaimer/filter',
  `response_template` TEXT COMMENT '触发时的回复模板',
  `description` VARCHAR(256) DEFAULT NULL,
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级(越小越优先)',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `regex_timeout_ms` INT UNSIGNED NOT NULL DEFAULT 50 COMMENT '正则超时(ms)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`rule_type`),
  KEY `idx_enabled_priority` (`is_enabled`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI安全规则配置';

CREATE TABLE `ai_safety_event` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_id` INT UNSIGNED NOT NULL COMMENT '触发的规则ID',
  `session_id` BIGINT UNSIGNED DEFAULT NULL,
  `user_id` BIGINT UNSIGNED DEFAULT NULL,
  `message_content` TEXT COMMENT '触发消息内容(截取前500字)',
  `action_taken` VARCHAR(16) NOT NULL,
  `response_sent` TEXT,
  `match_latency_ms` INT UNSIGNED DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rule` (`rule_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI安全事件日志';

CREATE TABLE `ai_prompt_template` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `template_key` VARCHAR(64) NOT NULL COMMENT 'system_prompt/chat_prompt/plan_prompt',
  `content` TEXT NOT NULL,
  `variables` JSON DEFAULT NULL COMMENT '模板变量列表',
  `version` INT UNSIGNED NOT NULL DEFAULT 1,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `activated_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_version` (`template_key`, `version`),
  UNIQUE KEY `uk_active` (`template_key`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Prompt 模板';

CREATE TABLE `ai_usage_daily` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL,
  `total_chat_sessions` INT UNSIGNED NOT NULL DEFAULT 0,
  `total_chat_messages` INT UNSIGNED NOT NULL DEFAULT 0,
  `total_plan_generated` INT UNSIGNED NOT NULL DEFAULT 0,
  `total_plan_confirmed` INT UNSIGNED NOT NULL DEFAULT 0,
  `total_tokens_used` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `positive_feedback_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `negative_feedback_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `satisfaction_rate` DECIMAL(5,2) DEFAULT NULL,
  `avg_response_time_ms` INT UNSIGNED DEFAULT NULL,
  `rag_hit_rate` DECIMAL(5,2) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI每日使用统计';

CREATE TABLE `ai_adjustment_config` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(64) NOT NULL,
  `config_value` VARCHAR(256) NOT NULL,
  `description` VARCHAR(256) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI微调规则配置';

-- 预设微调规则
INSERT INTO `ai_adjustment_config` (`config_key`, `config_value`, `description`) VALUES
('load_increase_completion_threshold', '95', '增负荷完成率阈值(%)'),
('load_increase_rpe_max', '7', '增负荷RPE上限'),
('load_increase_pct_min', '2.5', '最小增负荷幅度(%)'),
('load_increase_pct_max', '5', '最大增负荷幅度(%)'),
('load_decrease_completion_threshold', '80', '降负荷完成率阈值(%)'),
('load_decrease_pct_min', '5', '最小降负荷幅度(%)'),
('load_decrease_pct_max', '10', '最大降负荷幅度(%)'),
('overfatigue_rpe_threshold', '10', '过度疲劳RPE阈值'),
('load_increase_streak_weeks', '2', '连续达标周数触发增负荷'),
('exercise_pr_streak_count', '3', '连续PR次数触发动作进阶');
```

### 4.4 AI 知识表扩展字段

```sql
-- 小程序端已有 knowledge_base 表，管理后台需要额外字段
ALTER TABLE `knowledge_base` ADD COLUMN `vector_status` VARCHAR(16) NOT NULL DEFAULT 'pending'
  COMMENT '向量化状态: pending/indexing/indexed/failed';
ALTER TABLE `knowledge_base` ADD COLUMN `vector_model` VARCHAR(64) DEFAULT NULL
  COMMENT '使用的 Embedding 模型';
ALTER TABLE `knowledge_base` ADD COLUMN `vector_indexed_at` DATETIME DEFAULT NULL
  COMMENT '向量化完成时间';
ALTER TABLE `knowledge_base` ADD COLUMN `vector_error` TEXT DEFAULT NULL
  COMMENT '向量化失败原因';
ALTER TABLE `knowledge_base` ADD COLUMN `deleted_at` DATETIME DEFAULT NULL
  COMMENT '软删除时间';
```

### 4.5 Redis 使用规划

| Key 前缀 | 用途 | TTL |
|----------|------|-----|
| `sa:session:*` | Sa-Token 会话 | 30min (滑动续期) |
| `admin:perm:{userId}` | 用户权限缓存 | 30min |
| `ratelimit:chat:{userId}` | AI 对话限流计数器 | 24h |
| `ratelimit:plan:{userId}` | AI 计划生成限流 | 24h |
| `config:ai:*` | AI 模型配置缓存 | 10min |
| `cache:dashboard:*` | 看板数据缓存 | 5min |
| `safety:rules:version` | 安全规则版本号 | 永久 |
| `safety:rules:keywords` | Aho-Corasick 自动机序列化 | 永久(变更时更新) |

### 4.6 ER 关系图

```
admin_user ──N:1── admin_role
admin_user ──1:N── admin_operation_log
admin_user ──1:N── admin_login_log
admin_user ──1:1── user (通过 user_id 关联)

user ──1:N── workout_log ──1:N── workout_log_exercise ──1:N── workout_log_set
user ──1:N── body_metric
user ──1:N── checkin
user ──N:M── user_achievement ──N:1── achievement
user ──1:N── post ──1:N── comment / post_like
user ──1:N── ai_chat_session ──1:N── ai_chat_message
user ──1:1── user_fitness_profile
user ──1:N── ai_plan ──1:N── ai_plan_adjustment
user ──1:N── weekly_training_summary

workout_plan ──1:N── plan_day ──1:N── plan_exercise ──N:1── exercise
exercise ──N:M── body_part (exercise_body_part)
exercise ──N:1── exercise_category

knowledge_base ──1:1── vector_index (向量映射)
ai_safety_rule ──1:N── ai_safety_event
ai_prompt_template (按 template_key + version 唯一)
```

---

## 五、核心业务流程

### 5.1 认证流程 (Sa-Token JWT + RBAC)

```
前端                    后端 (Sa-Token)
  │                        │
  │  POST /auth/login      │
  │  {username, password}   │
  │ ──────────────────────→ │
  │                        │  1. 校验用户名密码 (BCrypt)
  │                        │  2. 检查 admin_user.status = 1
  │                        │  3. Sa-Token 创建会话 → 生成 JWT
  │  ← {token, userInfo}   │
  │                        │
  │  GET /api/xxx          │
  │  Header: Bearer {jwt}  │
  │ ──────────────────────→ │
  │                        │  1. Sa-Token 解析 JWT
  │                        │  2. 从 Redis 读取会话信息
  │                        │  3. 校验权限注解 @SaCheckPermission
  │  ← {data}              │
```

**RBAC 权限模型**：

```
admin_user ──→ admin_role ──→ permissions (JSON)

permissions JSON:
{
  "dashboard": ["view"],
  "user": ["view", "edit", "disable"],
  "plan": ["view", "create", "edit", "delete", "publish"],
  "ai_knowledge": ["view", "create", "edit", "delete"],
  "ai_chat": ["view", "flag", "edit"],
  "ai_safety": ["view", "create", "edit", "delete"],
  "system": ["view", "edit"],
  "admin": ["view", "create", "edit", "disable"]
}
```

**三层防线**：

| 层级 | 实现 | 作用 |
|------|------|------|
| 前端路由 | Vue Router 动态路由 + v-permission 指令 | 控制菜单可见性 + 按钮显示 |
| 网关层 | Sa-Token 路由拦截器 | 拦截未登录请求，校验 Token |
| 接口层 | `@SaCheckPermission("plan:edit")` 注解 | 精确到接口级别的权限校验 |

### 5.2 RAG Pipeline 完整流程

```
用户提问 (小程序端)
    │
    ▼
┌──────────────────┐
│  1. 安全预检       │  ← 匹配 ai_safety_rule (blocked_topic/sensitive_word)
│  SafetyFilter     │     命中 → 直接返回拦截回复
└────────┬─────────┘
         │ 通过
         ▼
┌──────────────────┐
│  2. Query 改写    │  ← LLM 改写口语化表达为标准检索 query
│  QueryRewriter    │     "练完腿疼咋办" → "训练后腿部肌肉酸痛处理方法"
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  3. 混合检索       │
│  ┌─────────────┐  │
│  │ 向量检索     │  │  ← Milvus: embedding 相似度 Top-K
│  └──────┬──────┘  │
│  ┌──────▼──────┐  │
│  │ 关键词检索   │  │  ← MySQL: FULLTEXT INDEX (ngram 分词)
│  └──────┬──────┘  │
│  ┌──────▼──────┐  │
│  │ RRF 融合排序 │  │  ← score = Σ 1/(k + rank_i), k=60
│  └─────────────┘  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  4. 重排序(可选)   │  ← BGE-Reranker 对 Top-N 结果精排
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  5. 上下文组装     │  ← 提取关键段落，拼装结构化上下文
│  ContextBuilder   │     包含: 知识库引用、动作库数据、来源标注
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  6. Prompt 拼装   │  ← 读取 ai_prompt_template 当前激活版本
│  PromptAssembler  │     变量替换: {user_profile_json} {rag_context} {query}
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  7. LLM 调用      │  ← SSE 流式调用外部 LLM API
│  LlmClient        │     超时 30s / 重试 3 次 / 降级
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  8. 安全后检       │  ← 检查 LLM 输出是否包含 forbidden_content
│  ContentSanitizer │     命中 → 过滤/替换/附加免责声明
└────────┬─────────┘
         │
         ▼
    返回给用户 + 保存对话记录 + 更新用户画像
```

### 5.3 安全规则匹配引擎

**双模式匹配架构**：

```
┌─────────────────────────┐    ┌─────────────────────────────┐
│  模式 A: 关键词匹配      │    │  模式 B: 正则匹配            │
│  (Aho-Corasick 自动机)   │    │  (预编译 + 超时保护)         │
│  · O(n) 复杂度           │    │  · 预编译为 RegExp 对象      │
│  · 支持数万关键词         │    │  · 单条超时 ≤ 50ms          │
│  · 内存自动机常驻         │    │  · ReDoS 检测               │
│  · 匹配耗时 < 1ms        │    │  · 匹配耗时 < 5ms           │
└────────────┬────────────┘    └──────────────┬──────────────┘
             └──────────┬─────────────────────┘
                        ▼
             ┌─────────────────────┐
             │  结果合并 & 优先级排序 │
             └──────────┬──────────┘
                        ▼
             ┌─────────────────────┐
             │  执行动作            │
             │  block/warn/filter  │
             │  /disclaimer        │
             └─────────────────────┘
```

**缓存与预编译策略**：

```
规则变更 (管理后台 CRUD)
    │ Redis Pub/Sub 通知
    ▼
各 AI 对话节点收到通知 → 重新加载规则 → 重建 AC 自动机 → 预编译正则 (< 100ms)

冷启动: 服务启动时从 DB 加载全部启用规则 → 构建内存结构
降级: Redis 不可用时，每 60s 从 DB 轮询一次
```

**ReDoS 防护**：

| 措施 | 说明 |
|------|------|
| 超时机制 | 每条正则执行上限 50ms，超时跳过并记录告警 |
| 入库校验 | 新增/编辑正则时自动测试 10 个边界用例 |
| 正则审计 | 定时扫描标记潜在 ReDoS 模式（嵌套量词） |
| 降级策略 | 正则引擎异常时降级为关键词匹配 |

### 5.4 向量化异步处理流程

```
管理后台操作 (新增/编辑/批量导入)
    │
    ▼
写入 MySQL (vector_status=pending)
    │
    ▼
推送消息队列 (Redis List / Stream)
    │
    ▼
Worker 消费
    ├── 调用 Embedding API (text-embedding-3-small)
    ├── 写入 Milvus Collection
    └── 更新 MySQL 状态 (indexed / failed)

状态流转: pending → indexing → indexed
                            ↘ failed → (retry 3次) → indexing

批量导入:
1. 前端上传文件 → 后端解析 → 批量写入 MySQL (status=pending)
2. 返回 job_id，前端轮询进度
3. Worker 逐条调用 Embedding API
4. 前端通过 GET /ai/knowledge/jobs/:jobId 查询进度
```

### 5.5 WebSocket AI 对话实时监控

**用途**：管理后台实时查看小程序端正在进行的 AI 对话。

**消息协议**：

```json
// 客户端订阅某会话
{ "type": "subscribe", "sessionId": 1024 }

// 服务端推送新消息
{
  "type": "new_message",
  "sessionId": 1024,
  "message": {
    "id": 5678, "role": "user",
    "content": "昨天练了胸，今天浑身疼正常吗？",
    "timestamp": "2026-05-28T16:32:00"
  }
}

// 服务端推送 AI 回复
{
  "type": "ai_response",
  "sessionId": 1024,
  "message": {
    "id": 5679, "role": "assistant",
    "content": "这是延迟性肌肉酸痛(DOMS)...",
    "ragSources": ["knowledge#12", "knowledge#8"],
    "feedback": null
  }
}
```

---

## 六、API 接口设计

### 6.1 接口规范

- **Base URL**: `/api/admin/v1`
- **认证**: JWT Token (Bearer)，Access Token 2h 滑动续期 + Refresh Token 7d
- **限流**: 100 req/min/user (Nginx + 应用层双重)
- **响应格式**:

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "pagination": { "page": 1, "pageSize": 20, "total": 156 }
}
```

**错误码定义**：

| 错误码 | 含义 |
|--------|------|
| 0 | 成功 |
| 1001 | 未登录 |
| 1002 | 无权限 |
| 1003 | 参数校验失败 |
| 2001 | 用户不存在 |
| 2002 | 用户已禁用 |
| 3001 | 计划不存在 |
| 4001 | 动作不存在 |
| 5001 | AI 知识库操作失败 |
| 5002 | 向量化失败 |
| 5003 | LLM 调用超时 |
| 5004 | 安全规则拦截 |
| 9999 | 系统异常 |

### 6.2 完整接口列表

#### 认证模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 管理员登录 |
| POST | `/auth/logout` | 退出登录 |
| POST | `/auth/refresh` | 刷新 Token |
| GET | `/auth/profile` | 获取当前管理员信息 |
| PUT | `/auth/password` | 修改密码 |

#### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/users` | 用户列表(分页/筛选/搜索) |
| GET | `/users/:id` | 用户详情 |
| PUT | `/users/:id` | 编辑用户 |
| PUT | `/users/:id/status` | 启用/禁用 |
| PUT | `/users/batch-status` | 批量启用/禁用 |
| PUT | `/users/:id/admin-status` | 设置/取消管理员 |
| GET | `/users/:id/workouts` | 用户训练记录 |
| GET | `/users/:id/body-metrics` | 用户身体数据 |
| GET | `/users/:id/achievements` | 用户成就 |
| GET | `/users/:id/ai-profile` | AI 健身画像 |
| GET | `/users/:id/ai-chats` | AI 对话历史 |
| GET | `/users/export` | 导出用户数据 |

#### 训练计划

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/plans` | 计划列表(`?ai_generated=1` 筛选) |
| POST | `/plans` | 创建计划 |
| GET | `/plans/:id` | 计划详情 |
| PUT | `/plans/:id` | 更新计划 |
| DELETE | `/plans/:id` | 删除计划 |
| PUT | `/plans/:id/status` | 上架/下架 |
| POST | `/plans/:id/copy` | 复制计划 |
| GET | `/plans/:id/stats` | 计划统计 |

#### 动作库

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/exercises` | 动作列表 |
| POST | `/exercises` | 新增动作 |
| GET | `/exercises/:id` | 动作详情 |
| PUT | `/exercises/:id` | 编辑动作 |
| DELETE | `/exercises/:id` | 删除动作 |
| GET/POST/PUT/DELETE | `/categories[/:id]` | 分类 CRUD |
| GET/POST/PUT/DELETE | `/body-parts[/:id]` | 部位 CRUD |
| POST | `/upload/media` | 上传媒体(COS 签名) |

#### 训练数据

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/analytics/workout-overview` | 数据概览 |
| GET | `/analytics/workout-trend` | 趋势数据 |
| GET | `/analytics/peak-hours` | 高峰时段 |
| GET | `/analytics/exercise-popularity` | 动作热度 |
| GET | `/analytics/plan-funnel` | 计划漏斗 |
| GET | `/workout-records` | 记录查询 |
| GET | `/workout-records/export` | 导出 |

#### 成就与打卡

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/achievements[/:id]` | 成就 CRUD |
| POST | `/achievements/:id/grant/:userId` | 手动授予 |
| GET | `/checkin/stats` | 打卡统计 |
| GET | `/checkin/leaderboard` | 排行榜 |

#### 社区管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/community/posts` | 动态列表 |
| PUT | `/community/posts/:id/status` | 审核动态 |
| PUT | `/community/posts/:id/pin` | 置顶 |
| GET | `/community/comments` | 评论列表 |
| DELETE | `/community/comments/:id` | 删除评论 |
| GET | `/community/reports` | 举报列表 |
| PUT | `/community/reports/:id` | 处理举报 |
| POST | `/community/users/:id/mute` | 用户禁言 |
| GET/POST/DELETE | `/sensitive-words[/:id]` | 敏感词 CRUD |

#### AI 知识库

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/knowledge` | 知识列表 |
| POST | `/ai/knowledge` | 新增知识(异步向量化) |
| GET | `/ai/knowledge/:id` | 知识详情 |
| PUT | `/ai/knowledge/:id` | 编辑知识 |
| DELETE | `/ai/knowledge/:id` | 删除知识 |
| POST | `/ai/knowledge/batch-import` | 批量导入 |
| GET | `/ai/knowledge/jobs/:jobId` | 任务进度 |
| GET/POST/PUT/DELETE | `/ai/knowledge/categories[/:id]` | 分类 CRUD |
| POST | `/ai/knowledge/rag-test` | RAG 测试 |
| GET | `/ai/knowledge/:id/vector-status` | 向量状态 |
| POST | `/ai/knowledge/:id/retry` | 重试向量化 |
| POST | `/ai/knowledge/reindex` | 全量重建 |

#### AI 对话监控

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/chat-sessions` | 会话列表 |
| GET | `/ai/chat-sessions/:id` | 会话详情 |
| GET | `/ai/chat-sessions/:id/messages` | 消息列表 |
| GET | `/ai/chat-sessions/:id/stream` | SSE 实时流(P2) |
| POST | `/ai/chat-sessions/:id/messages/:msgId/issue` | 标记问题 |
| PUT | `/ai/chat-sessions/:id/messages/:msgId/issue` | 更新问题 |
| POST | `/ai/chat-sessions/:id/messages/:msgId/to-knowledge` | 反哺知识库 |
| GET | `/ai/chat-feedback-stats` | 反馈统计 |
| GET | `/ai/chat-issues` | 问题列表 |
| PUT | `/ai/chat-issues/:id` | 处理问题 |

#### AI 计划管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/plans` | 计划列表 |
| GET | `/ai/plans/:id` | 计划详情 |
| GET | `/ai/plans/:id/adjustments` | 调整历史 |
| PUT | `/ai/plans/:id/status` | 更新状态 |
| POST | `/ai/plans/:id/convert-to-system` | 转系统计划 |
| GET | `/ai/plans/stats` | 计划统计 |
| GET | `/ai/adjustment-rules` | 微调规则 |
| PUT | `/ai/adjustment-rules` | 更新微调规则 |

#### AI 安全与规则

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/ai/safety-rules[/:id]` | 安全规则 CRUD |
| POST | `/ai/safety-rules/test` | 测试规则 |
| GET | `/ai/safety-events` | 安全事件日志 |
| GET/POST | `/ai/prompts[/:id]` | Prompt CRUD |
| PUT | `/ai/prompts/:id` | 编辑(创建新版本) |
| PUT | `/ai/prompts/:id/activate` | 激活版本 |
| GET | `/ai/prompts/:id/versions` | 版本历史 |
| POST | `/ai/prompts/:id/rollback` | 回滚 |

#### AI 数据分析

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/analytics/overview` | 总览 |
| GET | `/ai/analytics/chat-trend` | 对话趋势 |
| GET | `/ai/analytics/satisfaction-trend` | 满意度趋势 |
| GET | `/ai/analytics/knowledge-usage` | 知识库使用 |
| GET | `/ai/analytics/hot-questions` | 热门问题 |
| GET | `/ai/analytics/plan-stats` | 计划统计 |
| GET | `/ai/analytics/token-usage` | Token 消耗 |
| GET | `/ai/analytics/rag-hit-rate` | RAG 命中率 |
| GET | `/ai/analytics/response-time` | 响应时间 |
| GET | `/ai/analytics/issues` | 低质量回答 |

#### 系统设置

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/system/configs` | 参数列表 |
| PUT | `/system/configs/:key` | 更新参数 |
| GET/POST/PUT/DELETE | `/system/announcements[/:id]` | 公告 CRUD |
| GET | `/system/operation-logs` | 操作日志 |
| GET | `/system/ai-config` | AI 配置(脱敏) |
| PUT | `/system/ai-config` | 更新 AI 配置 |
| POST | `/system/ai-config/test-connection` | 测试连接 |

#### 权限管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST | `/admins[/:id]` | 管理员 CRUD |
| PUT | `/admins/:id/status` | 启用/禁用 |
| POST | `/admins/:id/reset-password` | 重置密码 |
| GET/POST/PUT/DELETE | `/roles[/:id]` | 角色 CRUD |
| GET | `/login-logs` | 登录日志 |

### 6.3 关键接口请求/响应示例

#### 批量禁用用户

```
PUT /api/admin/v1/users/batch-status
Authorization: Bearer {token}

Request:
{
  "user_ids": [1024, 1025, 1026],
  "status": "disabled",
  "reason": "违规内容发布"
}

Response:
{
  "code": 0,
  "message": "success",
  "data": { "total": 3, "success": 3, "failed": 0, "failed_ids": [] }
}
```

#### RAG 检索测试

```
POST /api/admin/v1/ai/knowledge/rag-test
Authorization: Bearer {token}

Request:
{ "query": "新手减脂怎么安排", "top_k": 5 }

Response:
{
  "code": 0,
  "data": {
    "results": [
      {
        "id": 12, "title": "新手减脂训练指南",
        "category": "training", "score": 0.92,
        "snippet": "对于新手减脂，推荐全身训练3天/周...",
        "source": "knowledge_base"
      },
      {
        "id": 45, "title": "徒手深蹲",
        "category": "exercise", "score": 0.85,
        "snippet": "徒手深蹲是新手减脂的基础动作...",
        "source": "exercise"
      }
    ],
    "rag_hit_rate": 1.0,
    "latency_ms": 156
  }
}
```

---

## 七、AI 模块技术方案

### 7.1 LLM 调用层

**统一客户端设计**：基于 OpenAI Java SDK，兼容 OpenAI / DeepSeek / Qwen 等兼容 API。

```java
// fitness-ai/.../llm/LlmClient.java
@Service
public class LlmClient {
    private final OpenAiApi api;
    private final LlmProperties props;

    // SSE 流式调用
    public Flux<String> chatStream(List<ChatMessage> messages) {
        return api.chatCompletionStream(new ChatCompletionRequest(
            props.getModel(), messages, props.getTemperature(), props.getMaxTokens()
        )).map(chunk -> chunk.choices().get(0).delta().content());
    }

    // 同步调用（用于 Query 改写等非流式场景）
    public String chat(List<ChatMessage> messages) {
        // 超时 30s，重试 3 次
    }
}
```

### 7.2 Embedding 向量化

```java
// fitness-ai/.../llm/EmbeddingClient.java
@Service
public class EmbeddingClient {
    // 调用 text-embedding-3-small 生成 1536 维向量
    public float[] embed(String text) {
        EmbeddingRequest req = new EmbeddingRequest(props.getEmbeddingModel(), text);
        return api.embeddings(req).data().get(0).embedding();
    }

    // 批量向量化（用于知识库批量导入）
    public List<float[]> embedBatch(List<String> texts) {
        // 批量调用，控制并发
    }
}
```

### 7.3 Milvus 向量数据库设计

**Collection Schema**：

```
Collection: knowledge_vectors
├── id           (INT64, primary key)
├── source_type  (VARCHAR) — "knowledge" / "exercise" / "plan"
├── source_id    (INT64)   — 来源表的 ID
├── category     (VARCHAR) — 知识分类
├── vector       (FLOAT_VECTOR, 1536 维)
└── created_at   (INT64)
```

**索引参数**：

```
Index: HNSW
├── ef_construction = 256
├── M = 16
├── metric_type = COSINE
└── 查询时 ef = 128
```

### 7.4 混合检索 RRF 融合算法

```java
// fitness-ai/.../rag/HybridRanker.java
@Service
public class HybridRanker {

    // Reciprocal Rank Fusion
    public List<SearchResult> fuse(List<SearchResult> vectorResults,
                                    List<SearchResult> keywordResults,
                                    int k) {
        Map<Long, Double> scores = new HashMap<>();
        // 向量检索结果
        for (int i = 0; i < vectorResults.size(); i++) {
            long id = vectorResults.get(i).getId();
            scores.merge(id, 1.0 / (k + i + 1), Double::sum);
        }
        // 关键词检索结果
        for (int i = 0; i < keywordResults.size(); i++) {
            long id = keywordResults.get(i).getId();
            scores.merge(id, 1.0 / (k + i + 1), Double::sum);
        }
        // 按融合分数降序排列
        return scores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(5)
            .map(e -> new SearchResult(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
```

### 7.5 Prompt 模板版本管理

**设计要点**：
- `ai_prompt_template` 表按 `template_key + version` 唯一
- 编辑时创建新版本（INSERT），不修改旧版本
- 激活某版本时：同 key 下其他版本 `is_active` 置 0，当前版本置 1（事务保证）
- `uk_active` 唯一约束防止多个 active 版本

```java
// fitness-ai/.../service/AiPromptTemplateService.java
@Transactional
public void activate(Long id) {
    AiPromptTemplate target = getById(id);
    // 将同 key 下其他版本置为非活跃
    lambdaUpdate()
        .eq(AiPromptTemplate::getTemplateKey, target.getTemplateKey())
        .ne(AiPromptTemplate::getId, id)
        .set(AiPromptTemplate::getIsActive, false)
        .update();
    // 激活目标版本
    target.setIsActive(true);
    target.setActivatedAt(LocalDateTime.now());
    updateById(target);
}
```

### 7.6 限流方案

**Redis 滑动窗口**：

```java
// fitness-common/.../aspect/RateLimitAspect.java
@Around("@annotation(rateLimit)")
public Object around(ProceedingJoinPoint point, RateLimit rateLimit) {
    String key = "ratelimit:" + rateLimit.key() + ":" + SecurityUtil.getUserId();
    long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
        redisTemplate.expire(key, rateLimit.window(), TimeUnit.SECONDS);
    }
    if (count > rateLimit.max()) {
        throw new BizException(ErrorCode.RATE_LIMIT_EXCEEDED);
    }
    return point.proceed();
}
```

**限流配置**：

| 资源 | 限流规则 | Key |
|------|---------|-----|
| AI 对话 | 50 次/用户/天 | `ratelimit:chat:{userId}` |
| AI 计划生成 | 3 次/用户/天 | `ratelimit:plan:{userId}` |
| RAG 测试 | 5 次/分钟 | `ratelimit:rag-test:{userId}` |
| API 总体 | 100 req/min/user | Nginx limit_req |

---

## 八、Docker 部署方案

### 8.1 Dockerfile（多阶段构建）

```dockerfile
# === 构建阶段 ===
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 POM 文件，利用 Docker 缓存层
COPY pom.xml .
COPY fitness-common/pom.xml fitness-common/
COPY fitness-user/pom.xml fitness-user/
COPY fitness-content/pom.xml fitness-content/
COPY fitness-workout/pom.xml fitness-workout/
COPY fitness-achievement/pom.xml fitness-achievement/
COPY fitness-community/pom.xml fitness-community/
COPY fitness-ai/pom.xml fitness-ai/
COPY fitness-system/pom.xml fitness-system/
COPY fitness-admin-app/pom.xml fitness-admin-app/

# 下载依赖（利用缓存层）
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY . .
RUN mvn package -DskipTests -pl fitness-admin-app -am

# === 运行阶段 ===
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装时区数据
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

COPY --from=builder /app/fitness-admin-app/target/fitness-admin-app.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
```

### 8.2 docker-compose.yml（本地开发）

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: fitness_admin
      MYSQL_CHARSET: utf8mb4
    volumes:
      - mysql-data:/var/lib/mysql
      - ./fitness-admin-app/src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./fitness-admin-app/src/main/resources/db/data.sql:/docker-entrypoint-initdb.d/02-data.sql
    command: --default-authentication-plugin=caching_sha2_password
             --character-set-server=utf8mb4
             --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data

  etcd:
    image: quay.io/coreos/etcd:v3.5.5
    environment:
      ETCD_AUTO_COMPACTION_MODE: revision
      ETCD_QUOTA_BACKEND_BYTES: "4294967296"
    volumes:
      - etcd-data:/etcd
    command: >
      etcd
      --advertise-client-urls=http://127.0.0.1:2379
      --listen-client-urls=http://0.0.0.0:2379
      --data-dir=/etcd

  minio:
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    ports: ["9000:9000", "9001:9001"]
    volumes:
      - minio-data:/minio_data
    command: minio server /minio_data --console-address ":9001"

  milvus-standalone:
    image: milvusdb/milvus:v2.4.1
    ports: ["19530:19530", "9091:9091"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    depends_on: [etcd, minio]
    volumes:
      - milvus-data:/var/lib/milvus

  backend:
    build: .
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: mysql
      DB_PORT: 3306
      DB_USER: root
      DB_PASS: root123
      DB_NAME: fitness_admin
      REDIS_HOST: redis
      REDIS_PORT: 6379
      MILVUS_HOST: milvus-standalone
      MILVUS_PORT: 19530
      COS_ENDPOINT: cos.ap-guangzhou.myqcloud.com
      COS_BUCKET: fitness-admin-media
      LLM_API_KEY: ${LLM_API_KEY}
      LLM_BASE_URL: https://api.openai.com/v1
      LLM_MODEL: gpt-4o
      SA_TOKEN_SECRET: ${SA_TOKEN_SECRET}
    depends_on: [mysql, redis, milvus-standalone]
    volumes:
      - ./logs:/app/logs

volumes:
  mysql-data:
  redis-data:
  etcd-data:
  minio-data:
  milvus-data:
```

### 8.3 Nginx 配置

```nginx
upstream backend {
    server fitness-admin-app:8080;
}

server {
    listen 443 ssl http2;
    server_name admin.example.com;

    ssl_certificate     /etc/ssl/certs/admin.example.com.pem;
    ssl_certificate_key /etc/ssl/private/admin.example.com.key;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        limit_req zone=api burst=50 nodelay;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }

    # Knife4j 接口文档（仅开发/测试环境）
    location /doc.html { proxy_pass http://backend; }
    location /webjars/ { proxy_pass http://backend; }
    location /v3/api-docs/ { proxy_pass http://backend; }

    limit_req_zone $binary_remote_addr zone=api:10m rate=30r/s;
}
```

### 8.4 CloudBase 部署拓扑

```
CloudBase 环境
│
├── ① 静态网站托管
│   └── fitness-admin-web (Vue dist/)
│       ├── 自带 CDN + SSL
│       └── 自定义域名绑定
│
├── ② 云托管 (容器化部署)
│   ├── fitness-admin-app (2C4G × 1)
│   │   ├── 端口: 8080
│   │   ├── 环境变量: DB/Redis/Milvus/COS/LLM 配置
│   │   └── 健康检查: /actuator/health
│   │
│   └── milvus-standalone (2C4G × 1)
│       └── etcd + minio 内部部署
│
├── ③ 云数据库 MySQL 8.0 (2C4G, 100GB SSD)
│   └── 主从架构 + 每日自动备份
│
├── ④ 云数据库 Redis 7 (2GB)
│   └── Sentinel 模式
│
├── ⑤ 云存储 COS
│   └── fitness-admin-media Bucket
│       ├── /exercise/  /achievement/  /knowledge/
│       └── CDN 加速
│
└── ⑥ 云函数 SCF (定时触发器)
    ├── ai-daily-stats (0 2 * * *)
    ├── workout-daily-stats (0 2 * * *)
    └── vector-reindex-check (0 3 * * 0)
```

### 8.5 环境变量配置

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| `DB_HOST` | MySQL 地址 | mysql |
| `DB_PORT` | MySQL 端口 | 3306 |
| `DB_USER` | MySQL 用户名 | root |
| `DB_PASS` | MySQL 密码 | root123 |
| `DB_NAME` | 数据库名 | fitness_admin |
| `REDIS_HOST` | Redis 地址 | redis |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `MILVUS_HOST` | Milvus 地址 | milvus-standalone |
| `MILVUS_PORT` | Milvus 端口 | 19530 |
| `COS_ENDPOINT` | COS 端点 | cos.ap-guangzhou.myqcloud.com |
| `COS_BUCKET` | COS Bucket | fitness-admin-media |
| `COS_SECRET_ID` | COS 密钥 ID | — |
| `COS_SECRET_KEY` | COS 密钥 | — |
| `LLM_API_KEY` | LLM API Key | sk-xxx |
| `LLM_BASE_URL` | LLM API 地址 | https://api.openai.com/v1 |
| `LLM_MODEL` | LLM 模型名 | gpt-4o |
| `EMBEDDING_MODEL` | Embedding 模型 | text-embedding-3-small |
| `SA_TOKEN_SECRET` | Sa-Token 密钥 | 随机字符串 |

---

## 九、安全方案

### 9.1 认证与鉴权

| 维度 | 方案 |
|------|------|
| 认证 | Sa-Token JWT (Access Token 30min 滑动续期 + 记住我 7d) |
| 鉴权 | RBAC: admin_user → admin_role → permissions JSON |
| 密码 | BCrypt 哈希存储，管理员密码独立于小程序微信登录 |

### 9.2 接口限流

| 层级 | 方案 |
|------|------|
| Nginx | limit_req 30r/s per IP，burst=50 |
| 应用层 | Redis 滑动窗口，按资源粒度限流 |
| AI 资源 | 每用户每日对话 50 次，计划生成 3 次 |

### 9.3 配置安全

- 敏感配置通过 CloudBase 环境变量注入，不写入代码/配置文件
- LLM API Key 等通过环境变量配置，管理后台展示时脱敏（`sk-••••••••`）
- AI 模型配置变更需超级管理员权限

### 9.4 操作审计

- `admin_operation_log` 记录所有写操作
- AOP 切面 `@Log` 注解自动记录（操作人/模块/类型/变更详情/IP/时间）
- 删除操作记录完整的变更前后对比（JSON）

### 9.5 AI 安全

| 措施 | 说明 |
|------|------|
| 双层过滤引擎 | 第一层：规则引擎快速匹配；第二层：LLM 语义审查（可选） |
| 安全事件日志 | `ai_safety_event` 记录所有被拦截/过滤的请求 |
| Prompt 注入检测 | 检测用户输入中的注入模式 |
| 免责声明 | 所有 AI 生成内容自动附加免责声明 |
| Token 预算 | 每日 Token 消耗统计 + 预算告警 |

### 9.6 传输安全

- HTTPS 全链路（CloudBase 自动 SSL 证书）
- WebSocket 使用 WSS（TLS）
- CORS 白名单控制

### 9.7 SQL 注入与 XSS

- MyBatis-Plus 参数化查询（禁止字符串拼接 SQL）
- 前端 DOMPurify 过滤 + 后端 XSS Filter

---

## 十、非功能性需求

### 10.1 性能指标

| 指标 | 目标值 |
|------|--------|
| API 响应时间 | < 300ms (P95) |
| 列表查询 | < 500ms (万级数据) |
| AI 对话首字响应 | < 2s |
| AI 计划生成 | < 15s |
| RAG 检索延迟 | < 300ms |
| 安全规则匹配 | < 5ms (P99) |
| 向量化单条 | < 3s |

### 10.2 可用性

- 服务可用性 ≥ 99.5%
- 数据库主从架构，自动故障转移
- Redis Sentinel 模式
- 健康检查 + 自动重启

### 10.3 可维护性

- 日志：Logback + 结构化 JSON 日志
- 监控：Spring Boot Actuator + CloudBase 日志
- 错误追踪：接入 Sentry
- API 文档：Knife4j 自动生成

---

## 十一、技术选型总览表

| 层级 | 技术栈 |
|------|--------|
| 语言 | Java 17 (Eclipse Temurin) |
| 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.6 |
| 认证 | Sa-Token 1.38 (JWT + RBAC) |
| 接口文档 | Knife4j 4.4 |
| 数据库 | MySQL 8.0 (CloudBase 云数据库) |
| 缓存 | Redis 7 (CloudBase 云数据库 Redis) |
| 向量数据库 | Milvus 2.4 (云托管 Docker) |
| 对象存储 | 腾讯云 COS |
| AI-LLM | OpenAI Java SDK (兼容 OpenAI/DeepSeek/Qwen) |
| AI-Embedding | text-embedding-3-small / BGE-M3 |
| AI-安全过滤 | 自研规则引擎 (Aho-Corasick + 正则) |
| 实时通信 | Spring WebSocket |
| 容器化 | Docker + 多阶段构建 |
| 反向代理 | Nginx |
| 部署 | CloudBase 云托管 + 静态托管 + 云数据库 |
| 定时任务 | Spring @Scheduled / CloudBase SCF |

---

## 十二、开发环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Eclipse Temurin 推荐 |
| Maven | 3.9+ | 构建工具 |
| Docker | 24+ | 容器开发 |
| Docker Compose | 2.20+ | 本地一键启动 |
| IDE | IntelliJ IDEA | 后端开发 |
| Node.js | 18+ / 20+ | 前端联调 |
| pnpm | 8+ | 前端包管理 |

---

## 十三、风险与应对

| 风险 | 等级 | 应对策略 |
|------|------|---------|
| 模块过多增加构建复杂度 | 低 | 统一父 POM 管理版本 |
| 跨模块循环依赖 | 中 | 严格单向依赖，通过接口解耦 |
| LLM API 调用延迟高 | 高 | SSE 流式返回 + Loading + 超时降级 (30s) |
| LLM API 费用超预算 | 中 | Redis 滑动窗口限流 + 每日 Token 统计 + 预算告警 |
| Milvus 运维复杂度 | 中 | 初期 Standalone 模式；定期备份 etcd + minio |
| RAG 召回质量不稳定 | 中 | RAG 测试面板定期评测 + 人工标记差结果反哺 |
| 向量索引与 MySQL 不一致 | 低 | 定时任务对比检查 + 事务保证 MySQL 先成功再同步 Milvus |
| CloudBase 实例冷启动 | 低 | 最小实例数 ≥ 1 |

---

## 十四、演进路径

```
Phase 1 (当前): 单体多模块 Spring Boot + Docker → CloudBase 云托管
    │   模块: common + user + content + workout + achievement + community + ai + system
    │   部署: 单 JAR 单容器
    │
    ▼
Phase 2 (用户量 > 10万):
    ├── 后端读写分离 (MySQL 主从)
    ├── Redis 集群模式
    ├── CDN 全面加速
    └── fitness-ai 可拆分为独立服务 (HTTP 内部调用)
    │
    ▼
Phase 3 (用户量 > 50万):
    ├── 各业务模块独立部署
    ├── 引入 API Gateway (Spring Cloud Gateway)
    ├── 引入 Nacos 注册/配置中心
    ├── Kubernetes 编排
    └── 完整微服务架构
```

> **设计原则**：当前阶段以简洁为主，单体多模块足够支撑业务；每个 Maven 模块职责边界清晰，后续可按需拆分为独立微服务，无需重构代码。

---

> **文档维护**: 需求变更时更新本文档并标注变更记录。  
> **联系方式**: [待填写]
