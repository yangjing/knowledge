# OpenSpec 测试 DSL (OSTD) 规范

> 测试成为 OPSX 中的一等公民 Artifact。

## 0️⃣ 工作流概览

```
Spec（人工编写 / AI 辅助）
  ↓
tests.*.md（AI 生成，人工可 Review）
  ↓
*.spec.ts（AI 生成，可执行）
  ↓
自动执行
  ↓
Spec + Test Gate
  ↓
Archive / Reject
```

---

## 1️⃣ DSL 命名规范

文件命名：

```txt
tests.<domain>.md
```

示例：

```txt
tests.user.md
tests.auth.md
tests.todo.md
```

---

## 2️⃣ DSL 核心结构（强制）

```md
# TestSpec: <领域标题>

@domain <domain-name>
@layer backend | frontend | e2e

## Scenario: <业务场景名称>

@id <unique-id>
@spec <spec-reference>

### Given

- ...

### When

- ...

### Then

- ...

### And

- ...
```

> **强制：@spec 必须引用 Spec 规则或章节**

---

## 3️⃣ Backend DSL 示例（Nest.js + Drizzle）

### `artifacts/tests.user.md`

```md
# TestSpec: User Profile

@domain user
@layer backend

## Scenario: Update profile with valid data

@id TS-USER-001
@spec user-profile.update.valid

### Given

- user is authenticated
- existing profile:
  - name: "Tom"
  - theme: "light"

### When

- PATCH /api/profile
- payload:
  - name: "Jerry"

### Then

- response status is 200
- response body.name is "Jerry"
- response body.theme remains "light"

---

## Scenario: Reject invalid theme

@id TS-USER-002
@spec user-profile.update.invalid-theme

### Given

- user is authenticated

### When

- PATCH /api/profile
- payload:
  - theme: "blue"

### Then

- response status is 400
- error.code is "INVALID_THEME"
```

---

## 4️⃣ Frontend DSL 示例（React 19 + AntD 6）

```md
## Scenario: Edit profile form submission

@id TS-USER-FE-001
@layer frontend
@spec user-profile.ui.edit

### Given

- user is on "Profile" page

### When

- user edits "Name" field to "Jerry"
- user clicks "Save"

### Then

- success message "Saved successfully" is shown
- profile name is updated to "Jerry"
```

> AI 可直接映射到 **Playwright + Testing Library**

---

## 5️⃣ 4 阶段 Agent 流水线

### Agent 角色

| Agent         | 输出                  |
| ------------- | --------------------- |
| SpecAgent     | `tests.*.md`          |
| TestGenAgent  | `*.spec.ts`           |
| TestExecAgent | 测试执行              |
| JudgeAgent    | Archive / Reject 决策 |

---

### 阶段 1：Spec → TestSpec

Prompt 模板：

```
You are a QA automation agent.

Input:
- spec.user.md

Task:
- Generate tests.user.md using OpenSpec Test DSL
- Cover all success and failure paths
- Each scenario must reference @spec
```

输出：`artifacts/tests.user.md`

---

### 阶段 2：TestSpec → 可执行测试

#### Backend (Nest.js)

```
You are a backend test generator.

Input:
- tests.user.md
- Nest.js app structure
- Jest + Supertest

Task:
- Generate Jest test files
- One scenario = one test case
- Use realistic API calls
```

输出：

```txt
tests/user/profile.update.spec.ts
```

#### Frontend (React 19)

```
You are a frontend test generator.

Input:
- tests.user.md
- React 19 + Ant Design 6

Task:
- Generate Playwright tests
- Use accessible selectors
```

---

### 阶段 3：自动执行（CI）

```bash
pnpm test              # 单元测试
pnpm test:e2e          # E2E 测试
```

Claude Code 将：

- 运行测试
- 收集失败信息
- 生成测试报告

---

### 阶段 4：Judge Agent 决策逻辑

```ts
if (allSpecsCovered && allTestsGenerated && allTestsPassed) {
  ARCHIVE = true;
} else {
  ARCHIVE = false;
}
```

---

## 6️⃣ OPSX Artifact 结构（完整版）

```txt
openspec/changes/user-profile/
├── artifacts/
│   ├── spec.user.md
│   ├── tasks.md
│   ├── tests.user.md     ← ⭐ 新增
│   ├── coverage.json     ← spec → test 映射
│   └── report.md         ← 测试执行报告
```

---

## 7️⃣ Spec ↔ 测试覆盖率表

`coverage.json`（自动生成）

```json
{
  "user-profile.update.valid": ["TS-USER-001"],
  "user-profile.update.invalid-theme": ["TS-USER-002"]
}
```

> Judge Agent 使用此文件检查是否漏测

---

## 8️⃣ 测试失败报告（自动生成）

`report.md`

```md
# Test Report

## Failed

- TS-USER-002
  - expected error.code INVALID_THEME
  - got UNKNOWN_ERROR

## Action Required

- Fix implementation or spec

## Passed

- TS-USER-001 ✓
```

---

## 9️⃣ Spec + Test Gate（质量检查点）

### Gate 规则（强制）

| Gate | 条件                                 |
| ---- | ------------------------------------ |
| G1   | Spec Review 通过                     |
| G2   | 每个 @spec 至少有 1 个 Test Scenario |
| G3   | `tests.*.md` 已 Review               |
| G4   | 所有测试通过                         |
| G5   | 无 Skip / TODO                       |

---

### Gate 决策矩阵

| Spec    | Test | 结果                        |
| ------- | ---- | --------------------------- |
| OK      | OK   | **Archive**                 |
| OK      | Fail | **Reject** (Implementation) |
| Bad     | OK   | **Reject** (Spec)           |
| Missing | -    | **Reject** (QA)             |

---

### CI Gate 示例

```yaml
- name: Spec Test Gate
  run: |
    node scripts/check-spec-coverage.js
    pnpm test
```

---

## 🔟 技术栈最佳实践

### Nest.js + Drizzle

- 使用 `TestDatabaseFactory`
- 每个测试场景：独立事务
- 测试后自动回滚

### React 19 + AntD 6

- 强制使用 `data-testid` 属性
- 表单场景优先使用 E2E（Playwright）

---

## 📝 一句话总结

> **在 OPSX 中：**
>
> - Spec 定义"应该发生什么"
> - Test 证明"确实发生了"
> - Code 只是中间产物
>
> **没有测试，不可 Archive。**

---

## 相关文档

- [Spec → 测试用例映射指南](./spec_to_test_mapping.md)
- [QA 评审清单](./spec_review_checklist_qa.md)
- [精简工作流](./opsx_trimmed_workflow.md)
