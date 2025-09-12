# select!

同时等 `tokio::sync::mpsc` 和 `kanal::Receiver`时，两者在 **`select!` 被取消时的语义不同**：

- **Tokio channel**：cancel-safe，不会丢消息，消息还在队列里，下次 `recv()` 还能取。
- **Kanal channel**：cancel-safe（内存安全），但消息可能在 _刚送达时_ 被 `Drop` 丢弃。也就是说 **应用层可能丢消息**。

---

## 解决思路

### 方案 1：不要在 `select!` 里直接用 `kanal::recv()`

Instead:

- 给每个 kanal receiver 开一个 **专门的任务**，只负责 `.recv().await` loop，不参与别的 `select!`。
- 把收到的消息通过 `tokio::mpsc` 转发到统一的调度逻辑里。

👉 这样就不会出现 select! drop 掉 kanal future 的情况。
这是最稳的方案，也符合 “channel → 消息聚合器 → select!” 的模式。

---

### 方案 2：把 `kanal::Receiver` 转成 `Stream`

Kanal 自己提供了 `.stream()` API（`Receiver::stream()` 返回 `impl Stream<Item = T>`）。
你可以用：

```rust
use futures_util::StreamExt;

let mut kstream = kanal_rx.stream();
tokio::select! {
    Some(msg) = kstream.next() => {
        // 永远安全，不会丢
    }
    Some(msg) = tokio_rx.recv() => {
        // 也安全
    }
}
```

`Stream` 的 `.next()` 语义比 `ReceiveFuture` 好：如果被 select! drop 掉，消息不会被写进来后又被丢弃（因为 poll_next 内部会处理 waker 注册，直到真正消费）。

---

### 方案 3：在 `select!` 里显式消费

如果你必须直接用 `kanal_rx.recv()`，那就要在分支里 **立即处理** `Some(T)`，不要让 Future 被提前 drop。
同时，最好在 select 外层套一个 loop，每次 select 后继续 re-register `recv()`。
但这样仍然存在 race condition：

- select 刚准备 poll `kanal_rx.recv()`，另一分支完成 → Future drop → 此时 kanal 消息正好送来 → 丢掉。
  所以这个方案风险大，不推荐。

---

## 推荐实践

✅ 最佳方案（推荐）：

- **Tokio channel** 直接 `recv()` 等就好。
- **Kanal channel** 转成 `Stream` 或放在单独任务 loop 里转发。
- 主逻辑只在 `select!` 里处理 `tokio::mpsc`，保证语义一致，不会丢消息。

---

要不要我帮你写一个 **Tokio + Kanal 混用的完整示例**，演示如何用一个 “聚合器任务” 把 Kanal 的消息安全地桥接到 Tokio 的 channel？

## tokio::select! 和 futures_util::select! 的区别

### 1. 两种 `select!` 的主要差别

#### `tokio::select!`

- **宏**，由 Tokio 提供。
- **行为**：

  - 一旦某个分支完成，其他分支的 Future **会被 drop**。
  - 默认是「公平」轮询（除非用 `biased;`）。
  - 适配 `tokio::mpsc::Receiver::recv()`，也能安全等待。

---

#### `futures_util::select!`

- 来自 `futures-util` crate。
- **行为**：

  - 默认也是：第一个完成的分支会执行，**其他分支 Future 会被 drop**。
  - 与 Tokio 版本不同的是，它有一些额外语法糖（`complete => break;`、`default => ...`）。
  - 它能和纯 `futures`/`async-std` 环境工作，不依赖 Tokio runtime。

---

### 2. 对 `tokio::mpsc` 的影响

- 在 **Tokio** channel 里，无论用哪种 `select!`：

  - 如果 `recv()` Future 被 drop，但消息已经进队列，**不会丢消息**；
  - 只要你重新 `.recv()`，还可以取到那条消息。

---

### 3. 对 `kanal::ReceiveFuture` 的影响

- **关键点**：

  - `kanal::ReceiveFuture` 如果被 drop，而此时发送方正好写入了数据 → 这条消息会被 `Drop` 逻辑销毁。
  - 这和你用 `tokio::select!` 还是 `futures_util::select!` **没有本质区别**，因为核心问题是：

    > **select! 的 loser 分支会被 drop，而 kanal 的 Future 在 drop 时可能丢掉刚送达的消息。**

---

### 4. 结论

- 用 **`tokio::select!`** 和 **`futures_util::select!`** 混用 `tokio::mpsc` 与 `kanal`，结果是一样的：

  - **Tokio channel 不会丢消息**（除非你自己丢掉 `Some(T)`）。
  - **Kanal channel 有可能丢消息**（因为 drop 时销毁已写入的数据）。

---

### 5. 推荐做法（无论哪个 `select!`）

✅ 一律不要把 `kanal_rx.recv()` 直接放进 `select!`。
👉 把 `kanal::Receiver` 转成 `Stream` (`rx.stream()`) 或者单独 task 转发到 tokio channel，再在 `select!` 里只操作 tokio channel。

这样 **无论你用 tokio::select! 还是 futures_util::select!，都不会丢消息**。

## 在`futures_util::select!`下的对照演示

- **Tokio channel (`tokio::mpsc`)**：消息不会丢。
- **Kanal channel (`kanal::unbounded`)**：消息可能因为 Future 被取消而丢失。

---

### 实验代码

```rust
use tokio::time::{sleep, Duration};
use tokio::sync::mpsc;
use kanal::unbounded;
use futures_util::{future::select, FutureExt}; // select! 的底层是 select()

#[tokio::main]
async fn main() {
    // === 1. tokio channel ===
    let (tx_tokio, mut rx_tokio) = mpsc::unbounded_channel::<&'static str>();
    // === 2. kanal channel ===
    let (tx_kanal, rx_kanal) = unbounded::<&'static str>();

    // === 3. 发送一条消息（稍微延迟）===
    let tx_tokio_clone = tx_tokio.clone();
    tokio::spawn(async move {
        sleep(Duration::from_millis(50)).await;
        tx_tokio_clone.send("tokio message").unwrap();
    });

    let tx_kanal_clone = tx_kanal.clone();
    tokio::spawn(async move {
        sleep(Duration::from_millis(50)).await;
        tx_kanal_clone.send("kanal message").unwrap();
    });

    // === 4. 构造两个 future，并用 futures_util::select 等待 ===
    let fut_tokio = async {
        let msg = rx_tokio.recv().await;
        println!("tokio got: {:?}", msg);
    }
    .boxed();

    let fut_kanal = async {
        let msg = rx_kanal.recv().await;
        println!("kanal got: {:?}", msg);
    }
    .boxed();

    // === 5. select 两个 future ===
    let result = select(fut_tokio, fut_kanal).await;

    match result {
        futures_util::future::Either::Left((_tokio_done, _kanal_future)) => {
            println!("tokio branch won, kanal future dropped!");
        }
        futures_util::future::Either::Right((_kanal_done, _tokio_future)) => {
            println!("kanal branch won, tokio future dropped!");
        }
    }

    // === 6. 再试一次 recv 看是否丢消息 ===
    println!("try tokio again: {:?}", rx_tokio.recv().await);
    println!("try kanal again: {:?}", rx_kanal.recv().await);
}
```

---

### 预期现象

1. **如果 tokio 分支赢了**：

   - 输出：

     ```
     tokio got: Some("tokio message")
     tokio branch won, kanal future dropped!
     try tokio again: None
     try kanal again: None
     ```

   - 说明：

     - `tokio::mpsc` 的消息正常被消费。
     - `kanal` 的 Future 被 drop → 消息在 drop 时被销毁 → 后续 `.recv()` 取不到。

2. **如果 kanal 分支赢了**（也可能出现）：

   - 输出：

     ```
     kanal got: Ok("kanal message")
     kanal branch won, tokio future dropped!
     try tokio again: Some("tokio message")
     try kanal again: None
     ```

   - 说明：

     - `kanal` 消息被正常消费。
     - `tokio::mpsc` 的 Future 被 drop，但消息还在队列里 → 下次 recv 还能取。

---

### 结论

- **Tokio channel**：cancel-safe，select! 输掉时消息不会丢，下次还在队列里。
- **Kanal channel**：如果 select! 输掉，Future drop 时可能销毁已到达的数据，导致应用层看起来像丢消息。

---

<!-- 要不要我再给你画一个 **时序图**，展示 select! drop 分支时，Tokio 和 Kanal 的消息命运差异？ -->
