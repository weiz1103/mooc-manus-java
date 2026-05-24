# MoocManus Java 架构手册

> 这份文档只有一个目标：把当前项目讲清楚。
>
> 适合 3 类场景：
> 1. 新同学快速理解项目；
> 2. 对外介绍项目架构；
> 3. 面试里讲项目时有一套稳定说法。

---

## 1. 先用一句话讲这个项目

`mooc-manus-java` 是一个基于 **Java 21 + Spring Boot + Spring AI + Redis + PostgreSQL** 的 Agent 后端系统。

它不是“调一下模型接口”的 Demo，而是一个真正按后端工程方式组织起来的系统：

- 前端通过 **SSE** 看执行过程；
- 后端把一次用户输入变成一个 **后台任务**；
- Worker 真正执行 Agent；
- 过程事件同时写入 **会话历史**、**任务事件账本** 和 **Redis Stream**；
- 支持 **断线续流**、**停止任务**、**循环识别**、**离线回放** 和 **执行评估**。

---

## 2. 这套系统主要解决什么问题

如果只做一个简单聊天接口，其实 Controller 调一下大模型就够了。

但 Agent 场景不一样，它通常会遇到这些问题：

- 一次任务可能执行很久；
- 中间会有很多步骤和工具调用；
- 前端希望实时看到过程；
- 用户断线后还想继续看；
- 任务可能失败、等待用户输入，或者被手动停止；
- 上线后还要能知道“这个 Agent 到底跑得好不好”。

所以这个项目的重点不是“让模型回答问题”，而是把 **模型能力、任务调度、事件流、状态机、可恢复性、可观测性** 放进一个清晰的后端架构里。

---

## 3. 当前模块怎么分工

### 3.1 `manus-api`

主应用服务，负责：

- REST / SSE 接口
- Session 管理
- 聊天请求接入
- 任务提交与停止
- Worker 调度
- 事件持久化
- 观测与评估接口

### 3.2 `manus-spring-ai`

Agent 运行内核，负责：

- Planner Agent
- ReAct Agent
- Planner + ReAct Flow
- Tool callback registry
- Memory / session state 抽象

### 3.3 `manus-common`

公共契约层，负责：

- `BaseEvent` 及各种 SSE 事件模型
- DTO
- 通用 `ToolResult`

### 3.4 `manus-sandbox-server`

侧边执行环境，负责：

- Shell
- 文件读写
- 浏览器能力
- 沙箱内执行环境

---

## 4. 一条主线：一次聊天请求是怎么跑完的

这是整个系统最重要的一条主线：

```text
前端发送消息
-> SessionController
-> ChatService
-> TaskDispatchQueue.submit(...)
-> AgentTaskWorker
-> AgentRunner
-> SpringAIPlannerReActFlow
-> AgentEventBus
-> EventPersister / SessionStateSync / SSE
```

把它展开后，就是下面这条更完整的链路。

### 第 1 步：前端发起聊天请求

接口仍然保持兼容：

- `/api/sessions/{sessionId}/chat`
- `/api/v1/sessions/{sessionId}/chat`

返回值仍然是 `text/event-stream`。

这点没有变，因为前端交互语义是冻结的。

### 第 2 步：`ChatService` 接住请求

`ChatService` 只做两类事：

1. **发送新消息**
2. **断线续流**

发送新消息时，它会先做几件前置动作：

- 校验会话是否存在；
- 校验当前会话是不是已经在运行；
- 做准入控制：频率限制、活动任务限制、24 小时配额；
- 记录用户消息；
- 为本轮对话准备 `Task` 和 `Sandbox`；
- 把用户消息先回显成 `message` 事件。

### 第 3 步：把请求变成后台任务

现在不是 HTTP 线程直接跑完整个 Agent 了。

`ChatService` 会把这次执行提交成一个 `AgentTaskCommand`，写进 Redis Stream 分发队列。

同时，会创建或更新一条 `TaskExecution` 记录，表示这次任务已经进入系统。

### 第 4 步：`AgentTaskWorker` 后台消费任务

Worker 会从 Redis Stream consumer group 中拉任务。

现在这条链路已经具备下面这些企业级特征：

- consumer group 消费
- pending list
- stale claim
- ack + delete
- worker 重启后可接管未完成消息

这意味着系统不再是“当前进程活着就能跑、挂了就丢”。

### 第 5 步：`AgentRunner` 真正驱动 Agent 执行

`AgentRunner` 负责：

- 调用统一 `AgentStrategy`
- 消费 Agent 产生的事件流
- 把事件写进 task output stream
- 做取消检查
- 把事件交给事件总线

如果执行出错，它会补 `error` 事件；
如果任务被取消，它会补 `done` 事件，保持前端终止语义兼容。

### 第 6 步：`SpringAIPlannerReActFlow` 负责 AI 执行过程

这一层是 Agent 运行内核。

核心思路是：

- 先由 Planner 生成计划；
- 再由 ReAct 按步骤执行；
- 执行过程中可以调用工具；
- 根据工具结果更新后续步骤；
- 最后总结输出。

所以它不是一轮“模型问答”，而是一个有状态的多步流程。

### 第 7 步：`AgentEventBus` 统一处理事件副作用

现在每个事件会按固定顺序走 3 件事：

1. **先持久化**
   - 会话历史
   - `task_event_log`
2. **再同步会话投影**
   - 标题
   - 状态
   - 未读数
   - 最新消息
3. **最后推 SSE**

这让系统里“事件落哪里、状态怎么改、前端何时看到”都走同一条路，不再散在多个类里。

---

## 5. 多条辅线：主线之外还有哪些重要能力

### 5.1 辅线 A：任务调度线

核心对象：

- `TaskDispatchQueue`
- `AgentTaskWorker`
- `TaskExecution`

职责：

- 提交任务
- 后台消费
- worker 接管
- lease / heartbeat
- stop / cancel

### 5.2 辅线 B：事件账本与回放线

核心对象：

- `task_event_log`
- `TaskEvaluationService`
- task output stream

职责：

- 记录任务完整事件过程
- 用于 replay
- 用于离线分析
- 用于问题排查

这里要特别区分两个概念：

#### `task_executions`

这是任务的**当前态快照**，比如：

- 当前状态
- 是否 loop
- tool 调用次数
- 最后一个事件 id
- 运行时告警

#### `task_event_log`

这是任务的**过程账本**，按事件一条条追加，用来做：

- 完整回放
- 审计
- 离线评估
- 复盘

一句话理解：

- `task_executions` 看“现在怎样”；
- `task_event_log` 看“过程发生了什么”。

### 5.3 辅线 C：观测评估线

核心对象：

- `ExecutionObservationSink`
- `InMemoryExecutionObservationSink`
- `TaskEvaluationService`

职责：

- 统计事件数
- 统计 tool 调用次数
- 识别重复工具调用
- 标记 loop 风险
- 给任务打分
- 输出 loop report

### 5.4 辅线 D：沙箱能力线

核心对象：

- `Sandbox`
- `Browser`
- `manus-sandbox-server`

职责：

- 执行 shell
- 文件读写
- 浏览器��作
- 给 Agent 提供安全可控的外部执行环境

---

## 6. 当前最关键的 4 个核心模型

### 6.1 `Session`

这是用户看到的会话。

它的对外状态保持简单：

- `pending`
- `running`
- `waiting`
- `completed`

这样做的好处是前端语义稳定。

### 6.2 `Task`

这是一次 Agent 执行对应的运行通道。

它背后主要挂着两条 Redis Stream：

- input / dispatch 侧
- output / replay 侧

### 6.3 `TaskExecution`

这是后台任务的执行快照。

内部状态更细：

- `SUBMITTED`
- `DISPATCHED`
- `RUNNING`
- `WAITING`
- `COMPLETED`
- `FAILED`
- `CANCEL_REQUESTED`
- `CANCELLED`

这个状态机主要服务于后端治理，不直接暴露给前端。

### 6.4 `BaseEvent`

这是整个系统里最稳定的协议模型。

前端能看到的 SSE 事件仍然是：

- `message`
- `plan`
- `title`
- `step`
- `tool`
- `wait`
- `error`
- `done`

这套事件契约现在是主边界，后端内部怎么改，都不能随便改它。

---

## 7. 当前可靠性是怎么做的

### 7.1 为什么前端断线后还能继续看

因为事件在推给前端前，已经先写进：

- Redis task output stream
- 数据库会话历史
- `task_event_log`

所以前端断线重连时，可以带着 `event_id` 继续读，不需要整轮重跑。

### 7.2 为什么 worker 挂了任务还能接回来

因为 Redis Stream dispatch queue 不是简单 list，而是 consumer group 模式。

已经取走但没 ack 的消息会进入 pending。
其他 worker 可以按 idle time claim 这些消息。

### 7.3 stop 为什么现在更统一

现在 stop 不再只是“直接改 Session 状态”。

而是：

1. 先写入 cancel intent；
2. 运行中的任务在执行循环里做协作式取消检查；
3. 如果任务还没启动，会直接补一个 `done`；
4. 内部状态统一落到 `TaskExecution`。

### 7.4 为什么 replay 更靠谱了

现在 replay 的主来源是 `task_event_log`。

`task_executions.replay_events` 还保留，但定位已经降成热缓存和兼容兜底，不再承担主账本职责。

---

## 8. 当前准入控制是怎么做的

现在已经补了 3 类基础准入规则，全部放在聊天提交入口：

1. **每分钟消息频率限制**
   - Redis 可用时走 Redis 计数窗口
   - Redis 不可用时退回进程内兜底
2. **单会话活动任务限制**
   - 默认一个会话同一时间只允许一个活动任务
3. **24 小时任务配额**
   - 直接从 `task_executions` 统计

重要的是：

- 前端接口没变；
- 被拒绝时仍然走 SSE；
- 表现形式仍然是 `error` + `done`。

所以用户体验不会突然断掉。

---

## 9. 当前观测与评估能做到什么程度

目前已经不是“只能看日志”了，而是有基础闭环：

### 在线可看

- 任务状态
- event 数
- tool 调用数
- step 数
- loop 风险
- warnings

### 离线可看

- `replay`
- `scorecard`
- `loop report`

也就是说，现在已经能回答下面这些问题：

- 这次任务执行到哪一步了？
- 是否重复调用了同一个工具？
- 最终是正常完成、失败、等待，还是取消？
- 这次执行质量大概如何？

---

## 10. 当前包结构怎么理解最顺

建议按下面顺序读代码：

1. `SessionController`
2. `ChatService`
3. `AgentTaskWorker`
4. `AgentRunner`
5. `SpringAIPlannerReActFlow`
6. `AgentEventBus`
7. `EventPersister`
8. `TaskExecutionService`
9. `TaskEvaluationService`

如果按这条顺序看，项目主线会非常清楚。

### `manus-api` 当前推荐理解方式

```text
interfaces      对外接口
service         应用编排
agent           执行入口与策略
event           事件副作用管道
domain          模型 / 仓储接口 / 外部端口
infrastructure  JPA / Redis / Sandbox / SpringAI 适配
observability   观测与评估
```

现在这套结构已经比之前清楚很多。

后面如果继续收敛，主要是把 `service` 里一部分应用编排再往更明确的 application 结构迁，但这一步不急，先保证主线稳定更重要。

---

## 11. 这个项目最适合怎么对外讲

### 11.1 30 秒版本

> 这是一个 Java 版的 Agent 后端系统。前端通过 SSE 看过程，后端把一次用户输入提交成后台任务，由 Worker 真正执行 Planner + ReAct Agent。执行过程中的事件会同时写入 Redis Stream、会话历史和任务事件账本，所以系统支持断线续流、任务回放、停止取消和执行评估。

### 11.2 3 分钟版本

> 这个项目最核心的点，不是接了 Spring AI，而是把 Agent 做成了一个真正可运行、可恢复、可观察的后端系统。用户发消息后，请求不会直接在 HTTP 线程里把整轮 Agent 跑完，而是先进入任务分发队列，由后台 worker 去消费。Agent 执行过程会不断产出 `message / plan / step / tool / wait / done` 这些事件。事件先落库、再更新会话状态、最后推给前端，同时 Redis Stream 还保存了续流所需的 event id。这样就把长任务、断线续流、失败恢复、停止取消和离线评估串成了一条清晰主线。

### 11.3 面试里最值得讲的亮点

建议重点讲这 5 个：

1. **前端语义冻结，后端渐进重构**
2. **同步执行改造成后台任务 + worker 消费**
3. **`TaskExecution` + `task_event_log` 双层模型**
4. **Redis Stream consumer group + pending + claim**
5. **loop detection + replay + scorecard**

---

## 12. 当前系统已经做到什么程度

### 已经完成的

- 后台任务化执行
- Worker recoverable 消费
- 统一 stop / cancel 语义
- `TaskExecution` 状态机
- `task_event_log` 事件账本
- replay / score / loop report
- 会话级限流 / 活动任务限制 / 配额
- 文档与主线收敛

### 还值得继续做的

- 更严格的 worker watchdog / lease 接管
- 更深层的工具执行取消传播
- Micrometer / OpenTelemetry
- 用户 / 租户级配额
- 死信队列 / poison message 治理
- 更完整的离线评测集

---

## 13. 最后给一个最简单的理解图

```text
一条主线：
前端请求 -> ChatService -> 提交任务 -> Worker 执行 -> EventBus -> 落库/投影/SSE

多条辅线：
- 调度线：dispatch queue / worker / cancel / claim
- 账本线：task_event_log / replay
- 观测线：snapshot / score / loop report
- 沙箱线：shell / file / browser
```

如果把这张图讲清楚，这个项目就已经能讲得很完整了。


