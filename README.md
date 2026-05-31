# MoocManus Java

`mooc-manus-java` 是一个面向 Agent 场景的 Java 后端项目。它做的事情很直接：**把一次用户输入变成可异步执行、可实时查看、可中断恢复、可回放评估的 Agent 任务。**

它不是一个只演示模型调用的 Demo，而是一套按后端工程方式组织起来的 Agent 系统。

---

## 项目简介

这个项目基于 **Java 21 + Spring Boot + Spring AI + Redis + PostgreSQL** 构建，前端通过 **SSE** 查看执行过程，后端把请求提交为后台任务，由 Worker 异步执行 Planner + ReAct 流程，并把执行事件同步写入会话历史、任务账本和事件流。

它重点解决的是下面这些问题：

- 一次 Agent 任务可能执行很久，不能一直占着 HTTP 线程
- 前端希望实时看到过程，而不是只拿最终结果
- 用户断线后，希望还能从上次的位置继续看
- 任务可能失败、等待输入，或者被手动停止
- 执行过程需要能回放、排查和评估

---

## 核心能力

当前系统已经具备这些核心能力：

- **SSE 实时推送**：前端可以持续看到 `message / plan / step / tool / done` 等事件
- **后台任务执行**：请求提交后由 Worker 异步消费，不在 HTTP 线程里长时间阻塞
- **断线续流**：客户端可以基于 `event_id` 从断点继续读取执行过程
- **停止与取消**：运行中的任务可以协作式取消
- **任务状态管理**：任务提交、分发、运行、等待、完成、失败、取消都有明确状态
- **事件账本与回放**：执行过程可 replay，可用于排查和复盘
- **基础观测与评估**：支持执行评分、循环风险识别和 loop report

---

## 模块结构

项目采用 Maven 多模块结构：

### `manus-api`
主应用模块，负责：

- REST / SSE 接口
- 会话管理
- 聊天主流程编排
- 任务提交与停止
- Worker 调度
- 事件持久化与状态同步
- 执行观测与评估接口

### `manus-spring-ai`
Agent 运行内核，负责：

- Planner Agent
- ReAct Agent
- Planner + ReAct Flow
- 工具回调注册
- Memory / session state 抽象

### `manus-common`
公共契约模块，负责：

- `BaseEvent` 及各类事件模型
- DTO
- 通用结果对象

### `manus-sandbox-server`
沙箱执行模块，负责：

- Shell 执行
- 文件读写
- 浏览器能力
- 受控执行环境

---

## 主链路

一次请求的核心执行链路如下：

```text
前端发消息
-> SessionController
-> ChatService
-> TaskDispatchQueue.submit(...)
-> AgentTaskWorker
-> AgentRunner
-> SpringAIPlannerReActFlow
-> AgentEventBus
-> EventPersister / SessionStateSync / SSE
```

一句话概括这条链路：

> 前端发起请求，后端把它变成后台任务执行，执行过程中持续产出事件，事件被保存、同步并推送给前端。

---

## 包结构

`manus-api` 当前按职责拆分为下面几层：

```text
manus-api
├─ interfaces
│  ├─ rest
│  └─ sse
├─ application
│  ├─ service
│  └─ agent
├─ domain
│  ├─ model
│  ├─ repository
│  ├─ service
│  ├─ external
│  └─ exception
└─ infrastructure
   ├─ event
   ├─ task
   ├─ repository
   ├─ external
   ├─ observability
   ├─ springai
   └─ config
```

这套结构的核心目的很简单：

- `interfaces` 负责对外接口
- `application` 负责流程编排
- `domain` 放核心模型和端口抽象
- `infrastructure` 放技术实现和外部适配

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9.15+
- PostgreSQL
- Redis

### 编译项目

```powershell
Set-Location "D:\Code\imooc-mas-master\imooc-mas\mooc-manus\mooc-manus-java"
mvn -q -DskipTests compile
```

### 运行测试

```powershell
Set-Location "D:\Code\imooc-mas-master\imooc-mas\mooc-manus\mooc-manus-java"
mvn -q test
```

### 启动 API 模块

主应用入口位于：

- `manus-api/src/main/java/com/imooc/manus/api/ManusApiApplication.java`

在补齐本地配置后，可直接启动该应用。

---

## 文档导航

如果你想继续深入看实现细节，建议按下面顺序阅读：

1. [`docs/README.md`](./docs/README.md)
2. [`docs/ARCHITECTURE_HANDBOOK.md`](./docs/ARCHITECTURE_HANDBOOK.md)
3. [`docs/API_CONTRACT.md`](./docs/API_CONTRACT.md)

---

## 总结

MoocManus Java 的重点，不是“让模型回答问题”，而是把 **任务执行、事件流、状态管理和前端实时交互** 这几件事真正串起来。

如果你想快速看懂这个项目，先从根 README 和架构文档开始；如果只记一句话，那就是：**它解决的是 Agent 在后端真正落地时的执行、续流、取消和回放问题。**
