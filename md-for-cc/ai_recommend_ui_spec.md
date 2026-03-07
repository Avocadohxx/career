# AI 推荐模块 UI 设计规范（补充）
> 本文件是对 `ui_design_spec.md` 的补充，聚焦于学生端「岗位推荐」和企业端「求职者推荐」的 AI 二级导航改造。
> 技术栈：Vue3 + Element Plus，大模型：豆包（字节 Doubao API）

---

## 一、改动范围

### 1.1 学生端侧边栏变更

```
原：
├── 岗位推荐

改为：
├── 岗位推荐
│   ├── 智能推荐      （原岗位推荐内容迁移至此）
│   └── AI 推荐
```

路由对应：
```
/student/recommend/smart        智能推荐（原 /student/recommend）
/student/recommend/ai           AI 推荐
```

### 1.2 企业端侧边栏变更

```
原：
├── 求职者推荐

改为：
├── 求职者推荐
│   ├── 智能推荐      （原求职者推荐内容迁移至此）
│   └── AI 推荐
```

路由对应：
```
/company/talent-recommend/smart     智能推荐（原 /company/talent-recommend）
/company/talent-recommend/ai        AI 推荐
```

---

## 二、智能推荐（无变化，仅迁移）

原有页面内容不变，直接迁移到二级路由，参考 `ui_design_spec.md` 中对应章节。

---

## 三、AI 推荐页面详细设计

### 3.1 整体布局

页面采用全屏对话框风格，顶部展示说明语，主体为对话消息流，底部为输入区。

```
┌─────────────────────────────────────────────────────┐
│  页面标题 + 说明语                                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│   消息流区域（可滚动，flex 列布局）                    │
│                                                     │
│   [AI 气泡]  系统初始消息（页面加载后自动触发）         │
│                                                     │
│   [用户气泡] 用户追问                                 │
│                                                     │
│   [AI 气泡]  AI 回复（含推荐卡片或纯文字）             │
│                                                     │
├─────────────────────────────────────────────────────┤
│  输入框（el-input textarea）  [发送按钮]              │
└─────────────────────────────────────────────────────┘
```

**布局参数：**
- 消息流区域：`height: calc(100vh - 260px)`，`overflow-y: auto`
- 消息气泡最大宽度：`70%`
- 输入框高度：`80px`，`resize: none`
- 页面整体 padding：`24px`
- 卡片背景：`#ffffff`，圆角 `8px`，`box-shadow: 0 2px 8px rgba(0,0,0,0.08)`

---

### 3.2 学生端 AI 推荐页（/student/recommend/ai）

#### 3.2.1 页面标题区

```
「AI 岗位推荐」
副标题：AI 已读取您的简历，将为您推荐最匹配的岗位
```

#### 3.2.2 自动触发逻辑（页面 onMounted）

页面加载后**无需用户操作**，前端自动向豆包 API 发起第一次请求：

**构造 prompt 所需数据：**

| 数据 | 来源 | 字段 |
|------|------|------|
| 学生技能标签 | resume 表（通过 student_id 关联） | `skills`（JSON 数组） |
| 学生自我评价 | resume 表 | `self_eval` |
| 全部有效岗位 | job 表（status = 已通过） | `title`, `skills`, `description` |

**前端 system prompt（固定，不展示给用户）：**

```
你是一个专业的就业推荐助手。
你会收到一名学生的技能标签和自我评价，以及当前系统中所有可投递岗位的信息。
请根据学生信息与岗位信息的匹配程度，推荐最合适的3~5个岗位。
每个岗位推荐需包含：岗位名称、推荐理由（1~2句话，结合学生技能与岗位要求说明匹配点）。
输出格式为 JSON 数组，不要输出其他内容，格式如下：
[
  {
    "job_title": "岗位名称",
    "reason": "推荐理由"
  }
]
```

**前端 user prompt（动态拼接）：**

```
学生技能标签：[从 resume.skills 读取，逗号拼接]
学生自我评价：[resume.self_eval]

当前可投递岗位列表：
[遍历 job 表，每条格式为：岗位名称：xxx；技能要求：xxx；岗位描述：xxx]
```

#### 3.2.3 AI 回复渲染

AI 返回 JSON 数组后，前端解析并渲染为**推荐卡片列表**，嵌入 AI 气泡内：

每张推荐卡片字段：
- 岗位名称（粗体，`font-size: 15px`）
- 推荐理由（灰色小字，`color: #909399`）
- 「立即投递」按钮（Element Plus 主色，`size: small`）

若 JSON 解析失败，直接将 AI 返回文本以纯文字气泡展示，不做卡片渲染。

#### 3.2.4 用户追问交互

用户可在输入框继续提问，例如"有没有上海的岗位"、"推荐一些薪资更高的"。

追问时将完整对话历史（messages 数组）拼接后传给豆包 API，保持上下文连续。

追问的 AI 回复：若包含岗位推荐仍按卡片渲染，否则按纯文字气泡展示。

---

### 3.3 企业端 AI 推荐页（/company/talent-recommend/ai）

#### 3.3.1 页面标题区

```
「AI 求职者推荐」
副标题：AI 将根据您发布的岗位需求，为您推荐最匹配的求职者
```

#### 3.3.2 自动触发逻辑（页面 onMounted）

页面加载后自动发起第一次请求。

**构造 prompt 所需数据：**

| 数据 | 来源 | 字段 |
|------|------|------|
| 企业发布的岗位 | job 表（company_id = 当前企业，status = 已通过） | `title`, `skills`, `description` |
| 全部学生简历 | resume 表 JOIN student 表 | `student.name`（脱敏）, `resume.skills`, `resume.self_eval` |

> 学生姓名脱敏规则：仅展示姓 + `**`，如「张**」，保护隐私。

**前端 system prompt（固定）：**

```
你是一个专业的招聘推荐助手。
你会收到一家企业发布的所有岗位信息，以及当前系统中所有学生的技能标签和自我评价。
请根据岗位需求与学生信息的匹配程度，推荐最合适的3~5名求职者。
每名推荐需包含：学生姓名（脱敏）、推荐理由（1~2句话，结合岗位需求与学生技能说明匹配点）。
输出格式为 JSON 数组，不要输出其他内容，格式如下：
[
  {
    "student_name": "张**",
    "reason": "推荐理由"
  }
]
```

**前端 user prompt（动态拼接）：**

```
企业岗位需求：
[遍历 job 表，每条格式为：岗位名称：xxx；技能要求：xxx；岗位描述：xxx]

当前求职者列表：
[遍历 resume JOIN student，每条格式为：姓名（脱敏）：xxx；技能标签：xxx；自我评价：xxx]
```

#### 3.3.3 AI 回复渲染

AI 返回 JSON 数组后，渲染为**求职者推荐卡片列表**，嵌入 AI 气泡内：

每张卡片字段：
- 学生姓名（脱敏，粗体）
- 推荐理由（灰色小字）
- 「查看简历」按钮 + 「发送面试邀请」按钮（`size: small`）

同样支持用户追问，如"有没有会 Python 的"、"推荐计算机专业的"。

---

## 四、豆包 API 对接规范

### 4.1 接口调用方式

调用统一走**后端代理**，前端不直接暴露 API Key。

前端请求后端接口：`POST /api/ai/chat`

请求体：
```json
{
  "messages": [
    { "role": "system", "content": "..." },
    { "role": "user", "content": "..." },
    { "role": "assistant", "content": "..." }
  ]
}
```

后端转发至豆包 API（`https://ark.cn-beijing.volces.com/api/v3/chat/completions`），返回结果透传给前端。

### 4.2 流式输出

建议启用流式输出（`stream: true`），前端使用 `EventSource` 或 `fetch` + `ReadableStream` 接收，实现逐字打印效果，提升体验。

打字效果实现：每收到一个 chunk 追加到当前 AI 气泡的 `content`，Vue 响应式自动更新。

### 4.3 Loading 状态

AI 响应期间：
- 发送按钮禁用，显示 loading 图标
- 消息流末尾展示「AI 正在思考中...」动画气泡（三个点跳动，CSS 动画实现）
- 响应完成后移除动画气泡，替换为正式内容

### 4.4 错误处理

| 场景 | 处理方式 |
|------|---------|
| API 调用失败 | AI 气泡展示：「抱歉，推荐服务暂时不可用，请稍后重试」 |
| JSON 解析失败 | 直接展示 AI 返回的原始文本，不渲染卡片 |
| 简历/岗位数据为空 | 自动触发前检查，为空则展示提示：「您的简历信息不完整，请先完善简历后再使用 AI 推荐」（学生端）/ 「您暂无已通过审核的岗位，请先发布岗位」（企业端） |

---

## 五、数据获取接口（前端需调用）

### 5.1 学生端 AI 推荐页初始化

```
GET /api/student/resume/my          获取当前学生简历（skills, self_eval）
GET /api/jobs/approved              获取所有已通过审核的岗位（id, title, skills, description）
```

### 5.2 企业端 AI 推荐页初始化

```
GET /api/company/jobs/approved      获取当前企业已通过审核的岗位（title, skills, description）
GET /api/admin/resumes/all          获取所有学生简历（student.name脱敏, skills, self_eval）
```

---

## 六、组件清单

| 组件名 | 说明 |
|--------|------|
| `<AiChatPanel>` | 通用 AI 对话面板，props: `systemPrompt`, `initUserPrompt`，emit: `send` |
| `<AiMessageBubble>` | 单条消息气泡，props: `role`（user/assistant）, `content`, `cards`（可选卡片数组） |
| `<JobRecommendCard>` | 学生端推荐岗位卡片，props: `jobTitle`, `reason`，emit: `apply` |
| `<TalentRecommendCard>` | 企业端推荐求职者卡片，props: `studentName`, `reason`，emit: `viewResume`, `invite` |
| `<AiThinkingBubble>` | 三点跳动 loading 气泡，无 props |
