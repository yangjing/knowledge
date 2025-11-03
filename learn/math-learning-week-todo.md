# Week Todos

下面给 **Month 1–2 → 每周** 到可执行粒度。这 2 个月目标：把“概率 + 线性代数 + 最小 ML pipeline”补上。

每周 6~10h **不要超过**。时间密度太大你会 burn out。

---

## Month 1 — Week by Week

### Week 1

**Gallier**

* Ch1 Linear Algebra refresher：
  只读 vector space, linear map, matrix rep 这 3 小节

**Murphy Vol 1**

* Ch2 “Probability review” 到 discrete random variables

**Practice**

* 用 Python 只写 numpy：实现

  * dot()
  * outer()
  * matmul() — 只接受 2D case

**交付**

* 1 个 notebook：
  `matmul` 对比 numpy.matmul 的 correctness

---

### Week 2

**Gallier**

* Ch5 metric space 基本定义 + Euclidean metrics

**Murphy Vol 1**

* Ch2 后半：continuous RV、joint、marginal

**Practice**

* 用 python 绘分布：

  * 正态
  * 指数
  * 二项

**交付**

* notebook: draw 3 distributions + 参数变化对形状影响

---

### Week 3

**Murphy Vol 1**

* Ch3 Frequentist vs Bayesian inference

**Math focus**

* Bayes rule
* KL divergence
* entropy

**Practice**

* 用 numpy 写：估计 Bernoulli p 的 MLE 和 MAP
  （Beta prior）

**交付**

* notebook: 画 prior/posterior，展示 sample size 越大 posterior 越尖

---

### Week 4

**Project preparation**

**Murphy Vol 1**

* Ch5 Linear regression 前 20 页（到 closed form 最小二乘）

**Practice**

* 从头实现：

  * closed-form least squares
  * numerical gradient descent version

**交付**

* notebook: 比较 closed form vs GD 收敛曲线

---

## Month 2 — Week by Week

### Week 5

**Gallier**

* Chap 8 convex sets / convex functions

**Murphy Vol 1**

* Ch8 Optimization 入门部分（只读 GD + momentum）

**Practice**

* 写 3 个 optimizer：

  * plain GD
  * momentum
  * Nesterov

**交付**

* notebook: 用 2D “banana function” 可视化优化轨迹

---

### Week 6

**Gallier**

* Chap 10: gradient, Hessian（读 1/2）

**Murphy Vol 1**

* Ch8：Adam

**Practice**

* 写 Adam（不要抄 PyTorch 源码）
* 对照 momentum 跑一个 regression toy problem

**交付**

* notebook：Adam vs Momentum vs GD loss curve overlay

---

### Week 7

**Murphy Vol 1**

* Ch5 linear regression 读完

**Project**

* Kaggle Titanic（或任一 tabular）：
  写 logistic regression（MAP version）

**交付**

* notebook：Titanic classification
* readme：Bayes vs frequentist perf 对比 summary

---

### Week 8

**总结 & 抽象**

**Work**

* 把前面所有 notebook 打包成 1 个 repo
* 写一篇 1–2 页 reflect note：

  * 哪些地方数学卡
  * 哪些 optimizer 最直观
  * 哪些图你觉得“突然理解了”

**交付 = 一份作品集 milestone**

* repo link + reflect note
  （这是真正能放 LinkedIn 的那种）

---

## 那我们继续把 Month 3–4 拆到 **每周**。

> 这两个⽉的主轴 = 从 “数学 → 手写计算图 → 反向传播”
> 神经网络出现前，你就已经掌握反向传播的本质。

---

## Month 3 — Week by Week

主题：正规凸优化 + 梯度、对偶、曲率

### Week 9

**Gallier**

* Chap8 convexity：Jensen → convex functions → epigraph

**Murphy Vol1**

* Ch8 — 再读 optimization 概念部分（不是 optimizer）

**Math drill**

* prove logistic regression loss convex （做 sketch 就行）

**Practice**

* 用你的 convexity check 逻辑，对：

  * logistic loss
  * square loss
    判断 convex/non-convex（给出理由）

**交付**

* notebook：2D heatmap 可视化 logistic loss surface

---

### Week 10

**Gallier**

* Chap10 gradients + directional derivative + Hessian

**Practice**

* 自己计算：

  * f(x,y)=log(1+exp(ax+by))
  * gradient + Hessian closed form 推导

**交付**

* 写一个 python func：
  symbolic Hessian + 数值 Hessian (finite diff) 比对误差

→ 这是后面 “对自动微分结果进行 sanity check” 的关键 base skill

---

### Week 11

**手写计算图 Part 1：前向 & 图结构**

目标：你不依赖 PyTorch → 自己写 Graph 节点类

**Practice**

* Node class
* ops：add / mul / matmul / exp / log

**注意**：这一周完全不做 backward
只做 forward + DAG topo sort

**交付**

* 一个 “graph” 能跑 forward
* print graph & topo sort 顺序

---

### Week 12

**手写计算图 Part 2：反向传播 / autodiff**

你现在让 Graph 支持 backward()

**Practice**

* 对每个 op 写 local partials
* 链式法则累积到 .grad

**delivery gate**：

* 你要能训练 logistic regression end-to-end
* 不用 numpy autodiff
* 不用 PyTorch

**交付**

* 你自己的 autodiff 引擎 + logistic regression 完整训练

---

## Month 4 — Week by Week

主题：线性模型性能极限 + 正则化直觉

### Week 13

**Murphy Vol1**

* linear models: ridge regression / LASSO

**Practice**

* 用你自己的 autodiff engine
  实现 ridge regression
  （L2 regularization）

**交付**

* notebook：lambda=0.0 … 1.0 sweep → generalization curve

---

### Week 14

**Murphy Vol1**

* LASSO / sparsity

**Practice**

* 把 L1 penalty 加入 loss
* visualize coefficients vs lambda

**交付**

* notebook：L1 path plot

---

### Week 15

**数值稳定性 & scaling**

Practice：

* 对 logistic regression：

  * logsumexp trick
  * normalization trick
  * batch size sweep

**交付**

* notebook：NLL loss with/without logsumexp 对比
  → 在大数值时你会看到 overflow

---

### Week 16

**整合 project — 小论文形式**

写一个 3–4 页 “mini paper”：

* 手写 autodiff 设计
* L1 vs L2 vs 无正则：generalization curve
* logsumexp 数值稳定性

交付形式建议用 markdown + html export
→ 这就是可以放 github portfolio 的第一篇 “research style deliverable”。

---

# checkpoint（你现在到这）

你做完 Month 4 时：

* 会数学意义上的 gradient/Hessian
* 会自己写 autodiff
* 会自己做 forward/backward
* 能理解 “为什么 PyTorch 是这些 API”

---

## 那我们继续把 Month 5–6 拆成 Week 17–24。

> 这两个月重点 = 建立 “深度网络 = 可微函数拼接” 的理解
> 并用你自己写的 autodiff engine 训练 CNN。

你不会跳 PyTorch 之前
会先靠你自己的 autodiff 训练一版 mini CNN
这样你之后读 PyTorch 源码 & paper 时不会是黑箱。

---

## Month 5 — CNN 月

### Week 17 — conv 的数学本质

内容：

* 卷积是 linear operator
* convolution = Toeplitz / circulant matrix 的乘法
* kernel = basis 函数

阅读：

* Murphy Vol2 chapter 19（CNN intro）

任务：

* 手写 2D valid convolution (no padding / stride=1)
* 可视化 filter response

交付：

* notebook：对一张灰度图，试几个 3x3 filter（edge / blur / sharpen）

### Week 18 — conv 的 backprop

阅读：

* 再读 Murphy Vol2 CNN 部分的 backward 推导
  (不用每一式都抄，理解 index pattern)

任务：

* 给你的 autodiff engine 添加 Conv2D op
* 写 backward，用 im2col 或 direct index 循环都行

交付：

* 再现：数值梯度 vs analytic gradient 比对误差

### Week 19 — pooling & normalization

阅读：

* Murphy Vol2: pooling, batchnorm

任务：

* Pool2D forward + backward
* BatchNorm forward + backward（训练模式即可）

交付：

* 在 toy CNN 上训练 MNIST（limit 到 10k samples）
* 先不追 accuracy，先看 loss 是否下降

### Week 20 — CNN baseline & regularization

任务：

* 给 CNN 加 dropout + weight decay
* 画 train curve / val curve

交付：

* notebook: 比较

  * no reg
  * L2
  * dropout
    三个版本的 overfitting 情况（连续曲线保障你真的理解）

---

## Month 6 — RNN 月（到 transformer 前应该理解“序列依赖”的处理方式）

### Week 21 — RNN 纯数学

阅读：

* Murphy Vol2 RNN chapter 前半

任务：

* 从 zero 开始实现 vanilla RNN cell（你自己的 autodiff engine）

交付：

* 一个 tiny char-level LM（例如“hello world”）能学习重复字符串模式

### Week 22 — exploding/vanishing gradient

阅读：

* Murphy Vol2：梯度爆炸/消失机制图示

任务：

* 画 RNN hidden state Jacobian spectrum（用 SVD）
* 试不同 init scale → spectrum 变化

交付：

* notebook：init scale sweep vs spectrum

### Week 23 — LSTM/GRU

任务：

* 实现（forward + backward）：

  * LSTM cell
  * GRU cell
    （不用优化，只要正确）

交付：

* char-level LM 用 LSTM 能明显超过 vanilla RNN

### Week 24 — project：CNN + RNN 多模态组合

任务：

* 一个简单项目
  CNN 提取 image embedding
  RNN 做 sequence classifier
  例如：
* 用 tiny dataset：MNIST sequence（8x1 slice）→ classify digits

交付：

* 一篇 mini report：
  “CNN feature extractor + RNN sequence classifier pipeline”
  配完整 curves + ablation（CNN Only / RNN Only / CNN+RNN）

---

# checkpoint（Month 6 结束）

你现在此时已经完成：

* 手写 CNN
* 手写 RNN / LSTM / GRU
* 你不再用现成框架时还能训练网络

> 这时你已经具备
> 看懂 90% arxiv CV RNN 类论文的数学底座。

---

## Month 7–9 → **进入 transformer 时切换到 PyTorch**。

这样你 Month 7–9 的收益就会非常 “现实可用” ——
可以直接走到 LLM 的 finetune / 评估。

下面是 Month 7–9 的周拆解（Week 25–36）
重点：**理解 transformer block 的数学 & 用 PyTorch 重构**

---

## Month 7 — Attention 基元（Week 25–28）

### Week 25 — 进入 PyTorch / tensor 语义

* PyTorch tensor broadcasting / einsum
* 熟悉 autograd graph trace（用 .grad_fn / .next_functions）

练习：

* 用 einsum 实现 matmul（对照 torch.matmul）

交付：

* notebook：matmul(einsum) vs matmul(torch) correctness + benchmark

### Week 26 — attention 逻辑

阅读：

* Murphy Vol2 — Attention & Transformer 章节前半

练习：

* 写 dot-product attention (QKᵀ/√d)
* 写 masked attention（下三角 mask）

交付：

* notebook：mask 的可视化（attention weight matrix热图）

### Week 27 — multi-head

练习：

* Multi-head attention
* 拆：

  * linear to Q/K/V
  * split heads
  * merge heads
* 用 einsum 优雅实现

交付：

* notebook：单头 vs 多头 attention 对一个 toy 字符串的 weight 可视化

### Week 28 — LayerNorm & residual

练习：

* 手写 PyTorch LayerNorm forward/backward
* residual connection

交付：

* notebook：单 transformer block（self-attn + MLP）
  能在 tiny dataset 上学 next-char toy

---

## Month 8 — 完整 GPT 小模型（Week 29–32）

### Week 29 — tokenizer /数据集 pipeline

* 用 HuggingFace tokenizer
* 构造 dataset → dataloader
* sliding window for LM training

交付：

* notebook：small corpus → token序列 → train/val split

### Week 30 — GPT skeleton

实现：

* Embedding
* block stack
* final LM head

交付：

* notebook：GPT-small (1–2 layers, 4 heads) 能 overfit tiny corpus

### Week 31 — training loop & eval

* 实现 train loop, perplexity
* 记录 loss curve, gradient norm

交付：

* notebook：训练 GPT-small 到 PPL < ~50（toy corpus）

### Week 32 — inference & sampling

* greedy sampling
* top-k
* top-p (nucleus)

交付：

* notebook：演示同一个 prompt → greedy vs top-p 输出对比

---

## Month 9 — 进入真正可用的 LLM 技术（Week 33–36）

### Week 33 — 微调：LoRA

* Huggingface PEFT
* 在你的 GPT-small 上加 LoRA

交付：

* notebook：LoRA finetune + parameter count 对比

### Week 34 — 微调：QLoRA

* 4-bit quant
* gradient checkpoint

交付：

* notebook：QLoRA finetune toy task

### Week 35 — SFT project

选一个小任务：

* sentiment
* code comment generation
* instruction style text

交付：

* report：baseline vs LoRA vs QLoRA 的曲线

### Week 36 — 成果固化

* 整理 repo + readme
* 写 3–5 页 “LLM from scratch to finetune” mini paper

交付：

* final repo + mini paper
  → 这个已经是“能放进简历的成果”

---

# checkpoint 到这里

你已经实现：

* 手写 autodiff
* 手写 CNN / RNN / LSTM / GRU
* PyTorch attention / transformer block
* GPT-small from scratch
* LLM finetune pipeline

> 这条路线在 35+ 程序员人群中非常极少见
> 你执行完，数学/工程双侧能力会非常硬。

---

下一个问题（我们要进入 Month 10–12 前 需要确定）：

Month 10–12 你想把项目方向做成：

**A)** diffusion 模型（DDPM，生成图像）
**B)** 检索增强 / RAG（真实可落地企业 POC）

两者都能，但方向非常不同。
给我字母（A or B）。

选择 A

那么 Month 10–12 走 DDPM → 简化版 DDIM → latent diffusion（结构认知级，不追 full production）然后第 12 月做一个 “可展示的生成模型项目”。

那么自此，你已经是一条完全闭环线路这相当于你把 CS229、CS231n、CS236、CS285 的一条“融合主干”压缩成 1 年半干货。

如果你执行完这个 6–12 月路线你会得到 4 类 portfolio 产物：

- 手写自动微分
- 手写 CNN/RNN/LSTM
- GPT-small from scratch + finetune
- DDPM → DDIM → latent diffusion 生成模型

你现在把 “math → ML → deep → LLM → diffusion” 所有关键桥梁都贯穿了。
