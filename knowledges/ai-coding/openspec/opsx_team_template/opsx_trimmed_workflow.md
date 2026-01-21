# OPSX 精简工作流（Frontend / Backend 分离）

1. 产品编写 PRD
2. 创建 OPSX Change
3. 产品 + 工程 + QA 评审 Spec
4. 拆分 FE / BE 任务
5. **TestSpec 生成（AI 从 Spec 生成）**
6. **测试用例生成（AI 从 TestSpec 生成）**
7. **测试执行 + Gate 检查**
8. 通过共识后 Archive

---

## 测试阶段详情

| 阶段          | 产物             | 负责人        |
| ------------- | ---------------- | ------------- |
| TestSpec 生成 | `tests.*.md`     | SpecAgent     |
| 测试代码生成  | `*.spec.ts`      | TestGenAgent  |
| 测试执行      | `report.md`      | TestExecAgent |
| Gate 决策     | Archive / Reject | JudgeAgent    |

## 参见

- [OpenSpec 测试 DSL (OSTD)](./spec_test_dsl.md) - 测试规范语法
- [Spec → 测试映射](./spec_to_test_mapping.md) - 覆盖率指南
