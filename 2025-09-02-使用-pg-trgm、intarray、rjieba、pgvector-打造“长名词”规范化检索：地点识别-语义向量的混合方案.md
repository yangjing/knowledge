title: 使用 pg_trgm、intarray、rjieba、pgvector 打造“长名词”规范化检索：地点识别 + 语义向量的混合方案
date: 2025-09-02 20:56:25
category:[ai]
tags:[pg_trgm, rjieba, pgvector, intarray, postgresql]

---

本文分享一个在生产中可落地的“长名词”规范化检索方案：以地点识别为核心，将中文分词与词典增强（[rjieba](https://github.com/messense/rjieba-py)）、PostgreSQL 的模糊匹配（[pg_trgm](https://www.postgresql.org/docs/17/pgtrgm.html)）、数组过滤（[intarray](https://www.postgresql.org/docs/17/intarray.html)）与语义向量（[pgvector](https://github.com/pgvector/pgvector)）进行混合召回与融合排序，兼顾召回率、可解释性与低延迟。

- 代码参考：
  - 地点级联模糊搜索函数：[aiguide-funcs.sql](../scripts/software/postgres/sqls/aiguide-funcs.sql)
  - 混合检索服务：[material_search_svc.py](../aiguide/domain/material/material_search_svc.py)
  - 地点模型与向量缓存序列化：[location_model.py](../aiguide/domain/location/location_model.py)

## 背景与问题定义

“长名词”常见于中文口语/社交文本，如“北京市圆明园遗址公园景区碧桐书院”和“北京圆明园碧桐书院”，“北京市故宫博物院坤宁宫”和“沈阳市沈阳故宫坤宁宫”这样的地点位置长名词。挑战在于：

- 别称/简称、行政区冗余、错别字、空格与标点差异；
- 用户意图中既有地点链路（省市区/园区/场馆），也有自由描述；
- 需要在模糊容错与结构化过滤之间取得平衡，并保证低延迟与可解释性；
- 相同的地名存在于不同的城市

## 技术选型概览

- `rjieba`：中文分词与自定义词典，优先长词和地点词，支撑“长名词”的切分与归一化。
- `pg_trgm`：三元组相似度（`%` 与 `<->`）提供容错匹配，支持别名、错别字与顺序扰动。
- `intarray`：基于 `int[]` 的交集过滤（`location_ids && ...`），将“识别出的地点链路”用于结构化剪枝。
- `pgvector`：`embedding <=> query_embedding` 进行语义相似度排序，兜底非精确文本匹配与跨表达召回。

## 系统架构（数据层—预处理—检索—融合）

- 数据层
  - `Location`: 存储树型结构的“地点”
  - `LocationAlias`: 存储“地点”的别名
  - `Material`（含 embedding 向量）: 存储属于“长名词”的素材的语义向量
- 预处理：并行执行“分词+地点识别”和“向量计算”，产出 location_ids 与 query_embedding。
- 检索：首先通过 rjieba 对长名词进行分词，然后通过 pg-trgm 进行模糊匹配，最后通过 intarray 进行结构化剪枝。获得标准化的 `location_ids`
  - 有 `location_ids` 时：地点过滤 + 语义向量排序；
  - 无 `location_ids` 时：纯向量降级策略。
- 融合：以 1 - 距离 作为基础分，可按需引入 trigram/别名命中加权与去重。

## rjieba 词典维护实践

- 词典结构与权重策略

  - 自定义词典采用“词条 词频 词性”格式（空格分隔），词频用于调控分词偏好，词性建议用“nz/nt/ns”等以突出专名；
  - 长词优先：对“园区/场馆/商圈/景区全称”设置更高词频，降低被切碎或被短词覆盖的概率；
  - 别名等价：将常见别称、简称、口语化写法全部入库，词频略低于标准名，避免别名在无必要时压制标准名。

- 数据来源与建库流程

  - 基础词库：来源于 Location 与 LocationAlias 表的 name/alias 字段，周期性全量导出；
  - 增量沉淀：采集线上查询日志与召回失败样本，挖掘 OOV（未登录词）并做人工校验后入库；
  - 行政区规范化：省/市/区/街道等层级名称统一规范（简繁、空格、标点），形成映射表供分词后归一。

- 热更新与版本管理

  - 词典按日期/版本号落盘，应用侧维护“当前版本号”；
  - 热更新流程：生成新词典文件 → 校验（格式、重复、冲突）→ 原子替换 → 加载到分词器（失败回退旧版本）；
  - 监控加载耗时与内存占用，避免在高峰期进行大体量重载。

- 消歧与冲突处理

  - 最长优先 + 业务白名单：对易混淆短词（如“中关村”、“天安门”）通过白名单强制长词优先；
  - 别名归一：分词后将别名映射为标准名的 canonical form，便于与 search_location 的候选比对；
  - 上下文约束：结合上位行政区（如“北京”→“朝阳区”）进行候选过滤，减少跨城误配。

- 与 search_location 的协同

  - 应用层将 rjieba 的 tokens（经归一化/去噪）以 text[] 传入 search_location(query_tokens)；
  - search_location 在每一层用 trigram（%/<->）对 location.name 与 location_alias.alias 排序，配合 k_window 做滑动窗口扩展；
  - 实践建议：
    - tokens 中优先保留“地点长名词”，去掉无信息助词；
    - 针对层级词（市/区/园区）保留原顺序，有助于递归链路拼接；
    - 对明显错别字的 token 进行简单归一化（同音/近形），降低 trigram 的噪声成本。

- 评估与监控指标

  - 词典覆盖率（对标标准名与别名）、OOV 比例、误切/漏切率；
  - 查询侧指标：地点识别成功率、最优链路深度分布、search_location 降级比例；
  - A/B：对比“仅向量”与“分词+地点识别+向量”的 nDCG、Hit@K 与人为可解释性。

- 性能优化要点

  - 词典按行政区/业务域分片，按需加载核心子集；
  - 缓存高频查询的分词结果与标准化链路；
  - 限制超长输入的 token 数量与长度，保护下游 SQL 的窗口枚举；
  - 定期清理低价值别名，避免词典“膨胀”影响分词速度。

- 示例：在应用层加载与使用 rjieba（简化示意）

```python
  import rjieba
  from typing import List

  # 启动时加载主词典与自定义词典
  rjieba.initialize()
  rjieba.load_userdict('/data/dicts/location_userdict.txt')  # 行内：词条 词频 词性

  def normalize_tokens(tokens: List[str]) -> List[str]:
    # 简单归一化示意：去空白/标点，别名到标准名映射（可查表）
    return [t.strip() for t in tokens if t.strip()]

  def tokenize_query(q: str) -> List[str]:
    # 长词优先，保留地点相关词
    words = [w for w, tag, freq in rjieba.tokenize(q, withFlag=True, HMM=False)]
    return normalize_tokens(words)

  # 传给 PostgreSQL 的 search_location(query_tokens) 作为 text[]
  # 例如：query_tokens := tokenize_query('去北京环球影城哈利波特魔法世界玩一天')
```

- 预处理流水线（并行）

- 分词与地点识别：
  - 先用 rjieba 切分并做“长词优先”的词典匹配；
  - 结合 pg_trgm 产生候选，弥补分词/拼写噪声；
  - 通过多级匹配+最长链路选择生成 location_ids（省 → 市 → 区 → 点）。
- 语义向量：并行计算 query 的 embedding，降低整体端到端延迟。
- 两路结果共同驱动后续 SQL 策略选择（见下文）。

## 检索阶段与 SQL 策略

混合检索的核心在 [material_search_svc.py](../aiguide/domain/material/material_search_svc.py)：

- 当识别到地点：
  - 使用 intarray 做结构化剪枝：location_ids && CAST(:location_ids AS int[])
  - 使用 pgvector 排序：embedding <=> CAST(:query_embedding AS vector)
  - 基础分为 (1 - distance)
- 无地点或低置信度：
  - 退化为“纯向量检索”，在不牺牲鲁棒性的前提下降级。

示例（节选，两类 SQL 思路）：

```sql
  -- 带地点过滤（简化示意）
  select id, (1 - (embedding <=> :query_embedding)) as score
  from material
  where status = 100
    and embedding is not null
    and location_ids && cast(:location_ids as int[])
  order by (embedding <=> :query_embedding)
  limit :limit;

  -- 纯向量（降级）
  select id, (1 - (embedding <=> :query_embedding)) as score
  from material
  where status = 100 and embedding is not null
  order by (embedding <=> :query_embedding)
  limit :limit;
```

此外，地点链路识别使用 SQL 存储函数进行“多级逐段匹配与扩展”，函数定义见：[aiguide-funcs.sql](../scripts/software/postgres/sqls/aiguide-funcs.sql) 的 search_location。其核心做法：按 token 窗口生成候选，对 location.name 与 location_alias.alias 分别用 % 过滤与 <-> 排序，逐级扩展形成最优链路。

## 数据库扩展与索引建议

```sql
  -- 启用扩展
  create extension if not exists pg_trgm;
  create extension if not exists intarray;
  create extension if not exists vector;

  -- 示例索引（按需调整字段名/策略）
  -- 1) trigram：地点名与别名
  create index if not exists idx_location_name_trgm on location using gin (name gin_trgm_ops);
  create index if not exists idx_location_alias_trgm on location_alias using gin (alias gin_trgm_ops);

  -- 2) intarray：素材表地点过滤
  create index if not exists idx_material_location_ids on material using gin (location_ids);

  -- 3) pgvector：语义向量近似检索
  create index if not exists idx_material_emb2 on material using ivfflat (embedding vector_cosine_ops) with (lists = 100);
```

实践要点：向量索引建议结合 probes 参数与定期重建；trigram 索引适合配合别名回表；数组索引能极大降低候选量。

## 性能优化与容量规划

- 并行化：分词/地点识别与向量计算并行，缩短 P95/P99。
- 剪枝：先地点过滤再向量排序；阈值提前过滤（score_threshold）。
- 批处理：DB 往返合并；必要时使用 pipeline 式多语句。
- 缓存：rjieba 词典、别名映射、热门地点 embedding 预热。

## 质量评估与监控

- 标注集：覆盖别称、错别字、行政区冗余与顺序扰动的查询。
- 指标：Recall/Precision、nDCG、Hit@K、空检率、降级比例。
- 监控：慢查询、相似度分布、错误日志与样本采样回放。

## 失败与降级策略

- 无地点命中：纯向量方案保证兜底召回。
- 向量服务不可用：trigram-only + 规则排序（别名优先、行政区近邻）。
- 超时：熔断与重试，返回可解释的兜底结果。

## 工程实践要点

- 模块边界清晰：预处理（分词/识别）→ SQL 组装 → 执行与解析 → 融合与日志。
- 可配置与热更新：词典、阈值、权重与索引参数。
- 一致性：别名变更引起的缓存失效策略；定期 Vacuum/Analyze。

## 案例走查（简）

- 输入："去北京环球影城哈利波特魔法世界玩一天"；
- 预处理：rjieba 产出长词候选，search_location(query_tokens:=['北京','环球影城','哈利波特','魔法世界']) 给出 location_ids 多级链路；
- 检索：优先使用地点过滤 + 向量排序；若地点未命中则走纯向量；
- 融合与输出：以 (1 - 距离) 为主分，并返回可解释的地点链路与命中依据。

## 附录

- 函数定义：[aiguide-funcs.sql](../scripts/software/postgres/sqls/aiguide-funcs.sql)
- 检索服务实现：[material_search_svc.py](../aiguide/domain/material/material_search_svc.py)
- 运行环境建议：PostgreSQL（pg_trgm、intarray、pgvector 已启用），Python 3.12+，rjieba 自定义词典按需加载。
