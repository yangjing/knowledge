# 一、完整示例：多 change → 单 apply（大型系统）

_非常好，这一组问题**已经是“OPSX 在真实复杂系统中的高级用法”**了。下面我会一次性给你一套 **可直接拿去用的完整方案**，不是概念说明。_

我们以一个**真实复杂但可控的系统**为例：

> 🎯 **SaaS 用户中心 + 偏好系统 + 权限系统**

---

## 1️⃣ 系统目标（背景）

- 多端（Web / API）
- 前后端分离
- 需要权限、状态、偏好一致性
- 非一次性生成，允许后续演进

---

## 2️⃣ 多 change 拆分策略（非常关键）

### 原则：

**一个 change = 一个“业务语义闭包”**

---

### 📦 Change 1：身份与认证（基础能力）

```txt
openspec/changes/
└── auth-core/
    ├── spec.auth.md
    ├── tasks.md
    ├── tests.auth.md
```

**定义：**

- 注册 / 登录
- access token 生命周期
- 未涉及用户资料

---

### 📦 Change 2：用户模型（领域对象）

```txt
└── user-core/
    ├── spec.user.md
    ├── tasks.md
    ├── tests.user.md
```

**定义：**

- User 是什么
- 基础字段（id / email / status）
- 不涉及 UI、不涉及偏好

---

### 📦 Change 3：用户偏好系统

```txt
└── user-preferences/
    ├── spec.preferences.md
    ├── tasks.md
    ├── tests.preferences.md
```

**定义：**

- theme / language / timezone
- 默认值规则
- 修改规则

---

### 📦 Change 4：权限系统

```txt
└── authorization/
    ├── spec.permissions.md
    ├── tasks.md
    └── tests.permissions.md
```

**定义：**

- role / permission
- 谁能改什么
- admin 与普通用户差异

---

### 📦 Change 5：用户资料 UI

```txt
└── profile-ui/
    ├── spec.profile.ui.md
    ├── tasks.md
    └── tests.profile.ui.md
```

**定义：**

- 页面行为
- 表单规则
- 不定义 API 实现细节

---

## 3️⃣ 单 apply 的结果（系统级一致性）

当你执行：

```bash
/opsx:apply
```

AI 实际做的是：

```text
auth-core
  ↓
user-core
  ↓
user-preferences
  ↓
authorization
  ↓
profile-ui
```

👉 自动构建一个**一致的世界观**：

- User 模型统一
- 权限规则全局一致
- API + UI 行为对齐
- 测试覆盖完整

---

# 二、Spec 冲突检测 Prompt（可直接用）

这是**你在 apply 前一定要跑的 AI 审查**。

---

## 🔍 Spec Conflict Detection Prompt

```text
You are an OpenSpec conflict detection agent.

Input:
- All spec files under openspec/changes/**/spec*.md

Task:
1. Identify conflicting definitions:
   - Field name conflicts
   - Behavioral contradictions
   - Default value mismatches
   - Authorization rule conflicts

2. Identify implicit dependencies between changes.

3. Output a conflict report with:
   - Conflict ID
   - Involved spec files
   - Description
   - Suggested resolution strategy

Rules:
- Do NOT propose implementation details.
- Focus only on semantic conflicts.
```

---

### 📄 输出示例（冲突报告）

```md
## Conflict C-001

Involved:

- spec.user.md
- spec.preferences.md

Description:

- spec.user.md defines `timezone` as required
- spec.preferences.md defines `timezone` as optional with default

Suggested Resolution:

- Move timezone ownership to preferences
- User core should reference it as derived
```

👉 **这个报告是“apply 的生死线”**

---

# 三、apply 前的自动验证脚本（Node.js）

这是你要的**工程级保障**。

---

## 📜 `scripts/pre-apply-validate.ts`

```ts
import fs from "fs";
import path from "path";

const CHANGES_DIR = "openspec/changes";

type SpecIndex = Record<string, string[]>;

function scanSpecs(): SpecIndex {
  const index: SpecIndex = {};

  const changes = fs.readdirSync(CHANGES_DIR);
  for (const change of changes) {
    const dir = path.join(CHANGES_DIR, change);
    const files = fs.readdirSync(dir).filter((f) => f.startsWith("spec"));

    for (const file of files) {
      const content = fs.readFileSync(path.join(dir, file), "utf-8");
      const matches = content.match(/@spec\s+([a-zA-Z0-9\.\-_]+)/g) || [];

      for (const m of matches) {
        const key = m.replace("@spec ", "");
        index[key] ??= [];
        index[key].push(`${change}/${file}`);
      }
    }
  }
  return index;
}

function validateNoDuplicateSpecs(index: SpecIndex) {
  const conflicts = Object.entries(index).filter(([_, files]) => files.length > 1);

  if (conflicts.length > 0) {
    console.error("❌ Duplicate spec definitions detected:");
    for (const [spec, files] of conflicts) {
      console.error(`- ${spec}:`);
      files.forEach((f) => console.error(`  - ${f}`));
    }
    process.exit(1);
  }
}

function validateTestCoverage() {
  const missing: string[] = [];

  const changes = fs.readdirSync(CHANGES_DIR);
  for (const change of changes) {
    const dir = path.join(CHANGES_DIR, change);
    const hasSpec = fs.readdirSync(dir).some((f) => f.startsWith("spec"));
    const hasTests = fs.readdirSync(dir).some((f) => f.startsWith("tests"));

    if (hasSpec && !hasTests) {
      missing.push(change);
    }
  }

  if (missing.length > 0) {
    console.error("❌ Missing tests for changes:");
    missing.forEach((c) => console.error(`- ${c}`));
    process.exit(1);
  }
}

console.log("🔍 Pre-apply validation started...");

const index = scanSpecs();
validateNoDuplicateSpecs(index);
validateTestCoverage();

console.log("✅ Pre-apply validation passed.");
```

---

## 在 package.json 中接入

```json
{
  "scripts": {
    "opsx:preapply": "ts-node scripts/pre-apply-validate.ts"
  }
}
```

然后：

```bash
pnpm opsx:preapply && /opsx:apply
```

---

# 四、哪些 change 可以合并 apply（决策表）

这是你问的**最实用的问题之一**。

---

## ✅ 可以合并 apply 的 change

| 条件         | 原因           |
| ------------ | -------------- |
| 同一业务域   | 减少跨域不一致 |
| 明确依赖链   | AI 能一次推导  |
| 无生产代码   | 风险低         |
| 新系统初始化 | 最优解         |

### 示例

```txt
auth-core + user-core + preferences → ✅ 合并 apply
```

---

## ❌ 不建议合并 apply 的 change

| 情况              | 原因     |
| ----------------- | -------- |
| 改动数据库 schema | 回滚困难 |
| 涉及外部系统      | 不可控   |
| 行为破坏性变更    | 风险集中 |
| 大型 UI 重构      | 难 debug |

---

## 🧠 推荐的 apply 分组策略

```txt
Phase 1 (Domain):
- auth-core
- user-core
- preferences

Phase 2 (Rules):
- authorization

Phase 3 (Experience):
- profile-ui
```

---

# 五、你现在已经站在什么位置（很重要）

你现在问的问题说明：

- 你已经**不是“AI 写代码使用者”**
- 而是在设计：

  > **“AI 作为工程执行体的操作系统”**

OPSX 正是干这件事的。

---

## 如果你愿意，下一步我可以直接帮你：

- 🧠 设计 **Change 依赖图（DAG）**
- 🧱 给你一套 **Spec 版本冻结 & 回滚机制**
- 🤖 写一组 **Claude Code 专用 OPSX Prompt Pack**
- 📦 给你一个 **“百 change 级”系统治理策略**

你现在这个阶段，**值得继续深挖**。
