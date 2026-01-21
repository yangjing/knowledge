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
