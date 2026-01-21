# OPSX 团队工作流模板

本模板集用于采用 OpenSpec + OPSX 的产品、研发、测试团队：把一次变更从“意图与范围”一路落实到“可实现、可验证、可沉淀”的系统事实（current truth）。

## 安装

```shell
npm install -g @fission-ai/openspec@latest
cd my-project
openspec init
openspec artifact-experimental-setup
```

## 目录结构与术语

- **capability（能力域）**：按能力域维护长期复用的 Spec（Requirement / Scenario）。
- **change（变更）**：一次变更的工作区，记录 why/what/how/acceptance，并产出对各 capability 的增量规格（delta specs）。
- **artifact（产物）**：在一个 change 里产生/维护的文件集合，如 proposal/specs/design/tasks。

对应到目录：

- `openspec/specs/`：系统事实（current truth），按 capability 组织的 Requirements / Scenarios（OpenSpec 定义）。
- `openspec/changes/<change-id>/`：变更工作区，包含 proposal/design/tasks 以及本次变更的 delta specs。
- OPSX 强调以 action 而非 phase 推进：proposal/specs/design/tasks 会随实现持续回写；最后通过 `/opsx:sync` 与 `/opsx:archive` 把变更沉淀回 `openspec/specs/`。

## 阅读路径（按角色）

- **产品（PM/PO）**：`proposal.md`（范围/影响）→ `specs/`（Scenario 是否覆盖关键路径）
- **研发（后端/前端/架构）**：`design.md`（决策/风险/迁移）→ `tasks.md`（落地路径）→ `specs/`（行为边界）
- **测试（QA/SET/SDET）**：`specs/`（Scenario 列表）→ `tasks.md`（验证项）→ 回看 `proposal.md`（范围防遗漏）

## 变更工作区：逐文件说明

### `.openspec.yaml`（元数据）

- 指定该 change 使用的 schema（如 `spec-driven`）与创建时间。
- schema 决定该 change 默认包含哪些 artifacts（proposal/specs/design/tasks 等），以及生成/校验规则（与 `opsx.md` 的 “Schema precedence / Artifact IDs by Schema” 保持一致）。

### `proposal.md`（意图与范围）

用于锁定共识，回答三件事：

- **Why**：为什么要做
- **What Changes**：对外可见的行为/规则/约束将如何变化
- **Impact / Capabilities**：影响哪些 capability（新增/修改/删除）

### `design.md`（技术决策）

把 proposal 中的“要改什么”落成可实现的决策（Decisions），并写清：

- 理由与替代方案
- 风险与约束
- 迁移与回滚

文档中的代码块用于表达约束与接口形态，属于示意，不会直接改变仓库代码。

### `tasks.md`（可执行清单）

把 proposal + design 拆成“按顺序可执行”的 checklist（实现时逐条勾选），通常包含：

- 文档与规范更新
- 后端/前端实现项
- 数据初始化/迁移项
- 验证与测试项（含必要命令，如 `bun run type-check`）

OPSX 的价值在于可回写：实现中发现不一致时，允许回到 proposal/design/specs/tasks 修订后继续推进。

### `specs/*/spec.md`（delta specs：增量规格）

目录：`specs/`

每个子目录对应一个 capability 的增量规格（delta spec），用 `## ADDED / MODIFIED / REMOVED` 结构表达本次变更对 Requirements/Scenarios 的增量。

示例：

- `platform-admin/spec.md`：平台管理员能力（如 org_type 支持、跨租户访问边界等）
- `role-permission-inheritance/spec.md`：角色边界、隐式继承、多角色权限并集等
- `user-auth/spec.md`：上下文切换刷新 Token、切换 API、旧 Token 失效等

这些 delta specs 负责把“对外承诺的行为”写成可验证的 Scenarios；随后通过 `/opsx:sync` 合并回 `openspec/specs/<capability>/spec.md`，成为新的系统事实（current truth）。

## 角色职责与归档门槛

- Spec 的业务语义正确性由产品负责把关。
- Spec 的技术可实现性与可验证性必须得到研发与测试认可。

| 维度                        | 主要负责 | 否决权      |
| --------------------------- | -------- | ----------- |
| 用户价值 / 业务规则         | 产品     | ✅          |
| 技术可实现性                | 研发     | ✅          |
| 可测试性 / 可判定性         | 测试     | ✅          |
| Spec 合并 / 归档（Archive） | 三方共识 | ❗ 必须一致 |

除非产品、研发、测试三方一致认可该变更满足以下条件，否则不得归档：

- 业务正确
- 可实现
- 可测试

> **Spec 不是权力工具，是减少扯皮的武器。**
> 如果 Spec 变成“产品说了算”，它很快会退化成 PRD；如果 Spec 需要三方共识，它会成为团队最值钱的资产。

## 可用模板和指南

### 核心工作流

- [OPSX 精简工作流](./opsx_trimmed_workflow.md) - 包含测试阶段的 Frontend/Backend 分离工作流

### 翻译与评审

- [PRD → OpenSpec 翻译模板](./prd_to_openspec.md) - 将 PRD 转换为 Spec 的模板
- [Spec 评审清单 - 工程](./spec_review_checklist_engineering.md) - 工程侧评审项
- [Spec 评审清单 - 产品](./spec_review_checklist_product.md) - 产品侧评审项
- [Spec 评审清单 - QA](./spec_review_checklist_qa.md) - QA 侧评审项

### 测试集成

- [OpenSpec 测试 DSL (OSTD)](./spec_test_dsl.md) - 完整的测试规范 DSL，包含 4 阶段 Agent 流水线
- [Spec → 测试用例映射](./spec_to_test_mapping.md) - 将 Spec 规则映射到测试用例的指南

### 冲突与边界

- [Spec 冲突解决流程](./spec_conflict_resolution.md) - 解决 Spec 冲突的流程
- [Spec 中不应包含的内容](./what_not_in_spec.md) - 保持 Spec 聚焦的指南

## 快速开始

1. 使用 `prd_to_openspec.md` 将 PRD 转换为 Spec
2. 使用 `spec_review_checklist_*.md` 进行评审
3. 使用 `spec_test_dsl.md` 生成测试
4. 三方共识后归档（Archive）

## 示例

- [多 change → 单 apply（大型系统）](./multi_change-single_apply.md)
