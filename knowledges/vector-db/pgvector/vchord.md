# Vchord 问题

# 索引选项：`lists` 和 `probes`

为什么将 vchordrq 索引的 `lists` 参数从 `[1024]` 改为 `[]` 后，SQLModel 查询就成功了。我需要分析 VectorChord 索引配置参数的含义，特别是 `lists` 参数对查询执行的影响。

根据 VectorChord 官方文档，问题的根本原因在于 **`lists` 参数和 `probes` 参数的配置关系**：

### 核心原因

1. **VectorChord 的 `lists` 参数决定了分区数量** <https://github.com/tensorchord/VectorChord>：

   - `lists = [1024]` 表示创建 1024 个分区
   - `lists = []` 表示不进行分区（适用于小数据集）

2. **`probes` 参数必须与 `lists` 参数匹配** <https://github.com/tensorchord/VectorChord>：
   - 当使用分区时（`lists = [1024]`），必须设置 `vchordrq.probes` 参数
   - 当不使用分区时（`lists = []`），不需要设置 probes 参数

### 官方推荐配置

根据数据量选择合适的 `lists` 配置 <https://github.com/tensorchord/VectorChord>：

| 数据行数 N         | 推荐分区数 L   | 示例 lists |
| ------------------ | -------------- | ---------- |
| N ∈ [0, 10⁵)       | 不适用         | `[]`       |
| N ∈ [10⁵, 2×10⁶)   | L = N/500      | `[2000]`   |
| N ∈ [2×10⁶, 5×10⁷) | L ∈ [4√N, 8√N] | `[10000]`  |

### 错误原因分析

原始配置 `lists = [1024]` 创建了 1024 个分区，但代码中没有设置相应的 `probes` 参数，导致查询时出现 `need 1 probes, but 0 probes provided` 错误。

修改为 `lists = []` 后，索引不再使用分区机制，因此不需要 probes 参数，查询自然成功。

### 解决方案

对于小数据集（< 100,000 行），使用 `lists = []` 是正确的选择。如果数据量较大需要使用分区，则应该：

1. 根据数据量设置合适的 `lists` 值
2. 在查询前设置相应的 `probes` 参数：`SET vchordrq.probes = <适当值>;`

推荐的 probes 值通常是 lists 数量的 3%-10% <https://pigsty.io/ext/rag/vchord/>。
