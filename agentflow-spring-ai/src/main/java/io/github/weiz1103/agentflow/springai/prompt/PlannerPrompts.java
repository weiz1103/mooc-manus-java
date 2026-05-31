package io.github.weiz1103.agentflow.springai.prompt;

/**
 * @author zhuang03@qq.com
 * @date 2026-05-31 08:36:11
 */
public final class PlannerPrompts {

    private PlannerPrompts() {}

    public static final String PLANNER_SYSTEM_PROMPT = """

你是一个任务规划智能体 (Task Planner Agent), 你需要为任务创建或更新规。
1. 分析用户的消息并理解用户的需。
2. 确定完成任务需要使用哪些工。
3. 根据用户的消息确定工作语言
4. 生成规划的目标和步骤
""";

    /** 创建计划提示词，{message} 。{attachments} 为占位符 */
    public static final String CREATE_PLAN_PROMPT = """
你现在正在根据用户的消息创建一个规。

注意。
- **你必须使用用户消息中使用的语言来执行任。*
- 你的规划必须简洁明了，不要添加任何不必要的细节
- 你的步骤必须是原子性且独立的，以便下一个执行者可以使用工具逐一执行它们
- 你需要判断任务是否可以拆分为多个步骤，如果可以，返回多个步骤；否则，返回单个步骤

返回格式要求。
- 必须返回符合以下 TypeScript 接口定义。JSON 格式
- 必须包含指定的所有必填字。
- 如果判定任务不可。 。steps"返回空数组，"goal"返回空字符串

TypeScript 接口定义。
```typescript
interface CreatePlanResponse {{
  /** 对用户消息的回复以及对任务的思考，尽可能详细，使用用户的语言 **/
  message: string;
  /** 根据用户消息确定的工作语言 **/
  language: string;
  /** 步骤数组，每个步骤包含id和描。**/
  steps: Array<{{
    /** 步骤标识。**/
    id: string;
    /** 步骤描述 **/
    description: string;
  }}>;
  /** 根据上下文生成的规划目标 **/
  goal: string;
  /** 根据上下文生成的规划标题 **/
  title: string;
}}
```

JSON 输出示例:
{{
  "message": "用户回复消息",
  "goal": "目标描述",
  "title": "任务标题",
  "language": "zh",
  "steps": [
    {{
      "id": "1",
      "description": "步骤1描述"
    }}
  ]
}}

用户消息:
{message}

附件:
{attachments}
""";

    /** 更新计划提示词，{plan} 。{step} 为占位符 */
    public static final String UPDATE_PLAN_PROMPT = """
你正在更新规划，你需要根据步骤的执行结果来更新规划：
{step}

注意。
- 你可以删除、添加或者修改规划步骤，但不要改变规划目。goal)
- 如果变动不大，不要修改描。
- 仅重新规划后。*未完。*的步骤，不要更改已完成的步骤
- 输出的步。ID 应以第一个未完成步骤。ID 开始，重新规划其后的步。
- 如果步骤已完成或者不再必要，请将其删。
- 仔细阅读步骤结果以确定是否成功，如果不成功，请更改后续步。
- 根据步骤结果，你需要相应地更新规划步骤

返回格式要求。
- 必须返回符合以下 TypeScript 接口定义。JSON 格式
- 必须包含指定的所有必填字。

TypeScript接口定义。
```typescript
interface UpdatePlanResponse {{
  /** 更新后的未完成步骤数。**/
  steps: Array<{{
    /** 步骤标识。**/
    id: string;
    /** 步骤描述 **/
    description: string;
  }}>;
}}
```

JSON输出示例。
{{
  "steps": [
    {{
      "id": "1",
      "description": "步骤1描述"
    }}
  ]
}}

步骤 (step):
{step}

规划 (plan):
{plan}
""";
}


