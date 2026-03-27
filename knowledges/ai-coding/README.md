# AI Coding

## 资料

- [你不知道的 Agent：原理、架构与工程实践](https://x.com/HiTw93/status/2034627967926825175) - 2026年3月19日
- [你不知道的 Claude Code：架构、治理与工程实践](https://x.com/HiTw93/status/2032091246588518683)

## CLAUDE.md or AGENTS.md 示例

- [claude.md](claude.md)
- [good-CLAUDE.md-example.md](good-CLAUDE.md-example.md)

### 让 Claude 维护自己的 CLAUDE.md

我最喜欢的一个技巧：每次纠正 Claude 的错误后，让它自己更新 CLAUDE.md：

> "Update your CLAUDE.md so you don't make that mistake again."
> 更新你的 CLAUDE.md，这样就不会再犯同样的错误了。

Claude 在给自己补这类规则时其实还挺好用，用久了确实越来越少犯同样的错。不过也要定期 review，时间一长总会有些条目慢慢过时，当初有用的限制现在未必还适合。

## AI 开发工作流

**是的！pm-skills + gstack 不仅可以结合使用，而且是目前社区公认的最强「全链路闭环」组合**（2026年3月24日最新确认）。

两者**零冲突、完美互补**，因为它们都基于统一的 **SKILL.md 标准**，只需把文件夹放进 `.claude/skills/` 就能同时加载。pm-skills 负责「做对的产品」（决策、框架、PRD、策略、风险分析），gstack 负责「快速安全落地」（工程评审、真实QA、Ship、文档同步）。

社区（Substack、Reddit、韩国/中文开发者帖）已经把它们称为「PM大脑 + 工程团队」的黄金搭档，有人甚至直接说「pm-skills 前半程 + gstack 后半程 = 一个人的 YC 级 startup 工厂」。

### 为什么完美兼容？

- 技能名称几乎不重叠（pm-skills 用 `/discover` `/write-prd` `/strategy` `/pre-mortem`；gstack 用 `/office-hours` `/plan-ceo-review` `/qa` `/ship`）
- 两者都支持自动加载 + 手动触发
- pm-skills 的输出（PRD、测试场景、发布笔记）可以直接喂给 gstack 的 `/office-hours` 或 `/document-release`
- pm-skills 有 8 个插件 + 65 技能；gstack 有 28 个角色技能 → 一起用就是 90+ 专业工具

### 30 秒安装（在已有项目里同时装两者）

```bash
# 1. 先装 pm-skills（Claude Cowork / Code 最佳方式）
claude plugin marketplace add --scope project phuryn/pm-skills
claude plugin install --scope project pm-product-discovery@pm-skills
claude plugin install --scope project pm-execution@pm-skills
# （建议至少装 discovery + execution + strategy 这 3 个插件）

# 2. 再装 gstack（保持你之前的安装）
cp -Rf ~/.claude/skills/gstack .claude/skills/gstack   # 如果还没装
cd .claude/skills/gstack && ./setup

# 3. 把 pm-skills 技能也拷贝到项目里（让团队共享）
mkdir -p .claude/skills/pm-skills
cp -r ~/.claude/skills/pm-* .claude/skills/pm-skills/ 2>/dev/null || true
```

然后 `git add .claude/ && git commit -m "feat: 同时启用 pm-skills + gstack 全家桶" && git push`

### 升级版 CLAUDE.md（直接替换你之前的版本）

把下面这段**完整替换**你项目根目录的 CLAUDE.md（或追加到最前面）：

```markdown
# CLAUDE.md（pm-skills + gstack 终极组合 • 全团队共享）

## 核心指令（必须遵守）

永远先用 pm-skills 做决策框架，再用 gstack 执行落地。

- 发现/策略阶段：优先 pm-skills 命令
- 评审/编码/QA/Ship：切换 gstack 角色
- 每次大变更后必须执行 /document-release（gstack）

## pm-skills（已启用）

可用命令：/discover, /write-prd, /strategy, /plan-launch, /growth-strategy, /pre-mortem, /test-scenarios, /release-notes, /north-star 等。
插件已加载：pm-product-discovery, pm-execution, pm-strategy（其余按需 /plugin:xxx）

## gstack（已启用）

Use /browse from gstack for ALL web browsing. Never use native chrome tools.
可用技能：/office-hours, /plan-ceo-review, /plan-eng-review, /plan-design-review, /qa, /ship, /land-and-deploy, /document-release, /retro, /autoplan, /freeze, /guard 等（完整列表见之前模板）

## 混合工作流强制规则

1. 新功能：先 `/discover` 或 `/write-prd`（pm-skills）→ 输出直接复制给 gstack
2. 然后 `/office-hours`（gstack）把 pm 输出作为输入
3. 评审：`/plan-ceo-review` + `/plan-eng-review` + `/design-review`
4. 执行：写代码 → `/review` → `/qa` → `/ship` + `/document-release`
5. 结束：`/retro`（gstack） + `/pre-mortem`（pm-skills）

## 项目 Specs 落地规则

所有 PRD/策略文档最终落 docs/design/（用 gstack /document-release 同步）
pm-skills 生成的内容也会被 gstack 自动更新到 ARCHITECTURE.md / README

**新人 clone 本 repo 后自动拥有两者全部技能！**
现在你可以直接输入 /discover 开始了。
```

### 真实混合使用案例（复制即用）

**场景：要做一个「AI 简报工具」新功能**

1. `/discover`（pm-skills）→ 生成完整 Opportunity Solution Tree + 假设列表
2. `/write-prd`（pm-skills）→ 输出结构化 PRD
3. 复制 PRD → 输入 `/office-hours`（gstack）→ AI 自动重构为 10-star 版本 + 设计文档
4. `/plan-ceo-review` + `/plan-eng-review`（gstack）→ 锁定范围和架构
5. `/strategy`（pm-skills）→ 补充增长/launch 计划
6. Claude 开始写代码 → `/review` + `/qa`（gstack）→ 真实浏览器验证
7. `/pre-mortem`（pm-skills）→ 风险检查
8. `/ship` + `/land-and-deploy` + `/document-release`（gstack）→ PR + 文档全更新

整个过程一条龙，你只需要说「yes」和提供 staging URL。

**进阶提示**

- 在 gstack 的 `/office-hours` 里说：「参考我刚用 pm-skills 写的 PRD：xxx」
- 用 pm-skills 的 `/test-scenarios` 生成测试用例 → 直接喂 gstack `/qa`
- 想更丝滑？把 `.claude/skills/` 整个提交到 Git，团队任何人拉代码就同时拥有两者。

**总结一句话**：
**可以，而且强烈推荐！** 这就是目前最接近「一个 AI 完成 PM 全职 + 工程全栈」的终极方案。pm-skills 保证「做对」，gstack 保证「做快且不翻车」。

你现在是用 Claude Code、Cursor 还是 Cowork？告诉我你的项目类型（新 SaaS / 已有产品 / side project），我立刻给你一份「今天就能跑的 8 步混合命令清单 + 完整项目模板」。随时说！🚀
