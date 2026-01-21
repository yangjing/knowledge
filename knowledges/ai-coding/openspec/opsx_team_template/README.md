# OPSX 团队工作流模板

本模板集包含采用 OpenSpec + OPSX 的产品/工程/QA 团队所需的模板和流程文档。

## 安装

```shell
npm install -g @fission-ai/openspec@latest
cd my-project
openspec init
openspec artifact-experimental-setup
```

## 可用模板和指南

### 核心工作流

- [OPSX 精简工作流](./opsx_trimmed_workflow.md) - 包含测试阶段的 Frontend/Backend 分离工作流

### 翻译与评审

- [PRD → OpenSpec 翻译模板](./prd_to_openspec.md) - 将 PRD 转换为 Spec 的模板
- [Spec 评审清单 - 工程](./spec_review_checklist_engineering.md) - 工程侧评审项
- [Spec 评审清单 - 产品](./spec_review_checklist_product.md) - 产品侧评审项
- [Spec 评审清单 - QA](./spec_review_checklist_qa.md) - QA 侧评审项

### 测试集成

- [OpenSpec 测试 DSL (OSTD)](./spec_test_dsl.md) - **新增** 完整的测试规范 DSL，包含 4 阶段 Agent 流水线
- [Spec → 测试用例映射](./spec_to_test_mapping.md) - 将 Spec 规则映射到测试用例的指南

### 冲突与边界

- [Spec 冲突解决流程](./spec_conflict_resolution.md) - 解决 Spec 冲突的流程
- [Spec 中不应包含的内容](./what_not_in_spec.md) - 保持 Spec 聚焦的指南

## 快速开始

1. 使用 `prd_to_openspec.md` 将 PRD 转换为 Spec
2. 使用 `spec_review_checklist_*.md` 进行评审
3. 使用 `spec_test_dsl.md` 生成测试
4. 通过共识后 Archive

## 示例

- [多 change → 单 apply（大型系统）](./multi_change-single_apply.md)

## Keywords

- **Spec 的“业务语义正确性”由产品拍板，**
- **Spec 的“可实现性与可验证性”必须得到研发和测试的共识。**

### Spec 谁都能写，但必须满足：

| 维度                | 负责角色 | 是否拥有否决权 |
| ------------------- | -------- | -------------- |
| 用户价值 / 业务规则 | 产品     | ✅ 是          |
| 技术可实现性        | 研发     | ✅ 是          |
| 可测试性 / 可判定性 | 测试     | ✅ 是          |
| Spec 合并 / Archive | 三方共识 | ❗ 必须一致    |

三方一致，才能成为系统事实

### 给你一个可以直接落地的团队规则（强烈推荐）

除非产品、工程和 QA 三方一致认可该规范符合以下条件，否则任何变更不得归档：

- 业务正确
- 可实现
- 可测试

---

> **Spec 不是权力工具，是“减少扯皮的武器”**
>
> 如果 Spec 变成“产品说了算”，它很快会退化成 PRD；
>
> 如果 Spec 需要三方共识，它会成为团队最值钱的资产。
