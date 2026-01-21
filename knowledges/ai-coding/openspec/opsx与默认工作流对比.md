下面是 **OpenSpec 官方默认工作流** 和 **最新的实验性工作流 OPSX**（基于官方文档和 v0.18+ 的发布说明）的对比总结，帮助你理解两者的区别和适用场景：([GitHub][1])

---

## 🧩 一、OpenSpec 默认工作流（当前稳定模式）

### 📌 核心目标

在正式写任何代码之前，让 **人类与 AI 明确达成一致的规范（spec）**，再由 AI 按规范执行，实现可控、可审查、可归档的开发过程。([GitHub][1])

### 🔄 标准流程

1. **Draft a Change Proposal**
   - 用 `/openspec:proposal` 或自然语言请求创建一个变更提案目录（包含 proposal.md、tasks.md、spec delta 等）
   - 作用：捕获需求、潜在影响、任务清单。([GitHub][1])

2. **Review & Align**
   - 审查并迭代 Proposal、Tasks、Spec Delta（规范增量）
   - 只有当提案被人类确认后，才进入实现阶段。([GitHub][1])

3. **Implement Tasks**
   - 用 `/openspec:apply <change-id>` 执行任务清单，AI 按任务和规范写代码。([GitHub][1])

4. **Archive**
   - 用 `/openspec:archive` 将变更归档：
     - 把变更的规范合并到主规范（系统的 Source of Truth）
     - 删除变更工作空间

   - 已归档的变更成为系统演进记录。([GitHub][1])

---

## 🧪 二、实验性工作流 OPSX（Experimental Workflow）

OPSX 是 OpenSpec 最近引入的 **更自由、更可定制的开发流**（目前主要为 Claude Code 等集成场景提供），目的在于：

🔹 **让工作流不再被固定指令「锁死」**
🔹 **允许开发者/团队自定义规范生成与执行顺序**
🔹 **不必严格按固定 4 步推进，而是更灵活地推进规范与实现**
🔹 **通过 schema + artifact 机制构造更细粒度的流程**
🔹 **可以实时编辑、调试模板/工作流策略，不需要 rebuild/重发版本**([GitHub][2])

### 🔧 OPSX 的主要特点

| 特性           | 默认工作流                          | OPSX 实验性                                                            |               |
| -------------- | ----------------------------------- | ---------------------------------------------------------------------- | ------------- |
| 指令锁定       | 稳定、不可改                        | 可定制流程指令和模式                                                   |               |
| 工作流阶段     | Proposal → Review → Apply → Archive | Artifact-driven、随时推进                                              |               |
| 顺序控制       | 有固定阶段与门（phase gate）        | 无“必须阶梯式推进”，可更新任意 artifact                                |               |
| 术语/Artifacts | proposal.md, tasks.md, spec deltas  | schema-aware artifacts + meta schemas                                  |               |
| 编辑灵活性     | 内建流程规则，不易更改              | 自由编辑“流程模板”并即时生效                                           |               |
| 支持命令扩展   | /openspec:proposal /apply /archive  | 新命令：/opsx:new /opsx:continue /opsx:ff /opsx:apply /opsx:archive 等 |               |
| 变更控制       | 基于规范 delta 合并                 | 支持 schema 同步、快速推进、合并策略更细粒度                           | ([GitHub][2]) |

### 🔄 OPSX 工作方式（简化理解）

在 OPSX 模式下，你可以像这样推进工作：

1. **新建变更（/opsx:new）**
   - 开始一个变更，不再马上生成所有 artifacts，而是生成基本骨架。([GitHub][2])

2. **继续推进（/opsx:continue）**
   - 支持逐步推进流程中各 artifact（比如先写 spec delta、再写 design、再写 tasks）
   - 不需要一口气生成全部内容。([GitHub][2])

3. **Fast-Forward（/opsx:ff）**
   - “快进”所有 planning artifact：
   - 一键生成完整 proposal、spec delta、tasks 和任何定义中的 artifact。([GitHub][2])

4. **实现/合并（/opsx:apply、/opsx:archive）**
   - 与默认流程类似，但支持 schema 驱动、自定义行为同步等更复杂策略。([GitHub][2])

### 💡 OPSX 的核心优势

- **灵活且可迭代**：不需要严格等到 proposal 完成才写 spec delta 或 tasks，可以边写边推进。([GitHub][2])
- **自定义优先**：开发者可以修改 OPSX 模板或 schema 定义来适配自己的团队习惯。([GitHub][2])
- **更智能的 artifact 管理**：支持 schema aware 操作，比如将特定元数据、模版绑定到 change 目录下。([GitHub][2])

---

## 🔍 三、关键差异总结

| 维度              | OpenSpec 默认工作流                                  | OPSX 实验性                               |               |
| ----------------- | ---------------------------------------------------- | ----------------------------------------- | ------------- |
| **流程结构**      | 固定四阶段（Proposal → Review → Apply → Archive）    | Artifact 可自由推进，阶段非强制顺序       |               |
| **规范生成**      | 一次性生成 proposal + tasks + spec delta             | 可分步生成、随时编辑                      |               |
| **灵活性**        | 明确但较刚性                                         | 高自由度，可定制行为和模板                |               |
| **命令集**        | /openspec:proposal /openspec:apply /openspec:archive | /opsx:new /opsx:continue /opsx:ff …       |               |
| **Schema / 模板** | 内置、难改变                                         | 支持 schema 驱动、开发者可编辑模板        |               |
| **集成深度**      | 稳定、可跨工具通用                                   | 更适合集成环境（如 Claude Code 深度场景） | ([GitHub][1]) |

---

## 📌 什么时候用哪种工作流？

| 需求                                     | 推荐                     |               |
| ---------------------------------------- | ------------------------ | ------------- |
| 稳定、可审计、团队共享规范驱动开发       | **默认 OpenSpec 工作流** |               |
| 需要在流程中快速迭代规范、任务或模板     | **OPSX 实验性工作流**    |               |
| 需要支持团队自定义行为或扩展命令         | **OPSX 模式**            |               |
| 更喜欢结构化、可复现的“先规范后实现”流程 | **默认 OpenSpec**        | ([GitHub][2]) |

---

如果你需要，我还可以给出一个结合 **Claude Code + OPSX** 的 **实际示例操作流程（带命令与文件内容模板）**。只要告诉我你想实现的功能就行！

[1]: https://github.com/Fission-AI/OpenSpec?utm_source=chatgpt.com "GitHub - Fission-AI/OpenSpec: Spec-driven development (SDD) for AI coding assistants."
[2]: https://github.com/Fission-AI/OpenSpec/releases?utm_source=chatgpt.com "Releases · Fission-AI/OpenSpec"
