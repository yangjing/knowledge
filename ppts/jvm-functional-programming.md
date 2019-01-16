title: JVM函数式编程
speaker: 杨景（羊八井）
url: https://www.yangbajing.me/shares/publish/jvm-functional-programming.md
files: /css/moon.css,/img

[slide]

# JVM函数式编程
<p style="text-align:right">分享人：杨景</p>

[slide]

# JVM的函数式编程

- **思维转变**
- **Lambda表达式**
- **Stream**
- **方法引用**
- **高阶函数**
- **ComplatableStage的组合**
- **Scala的函数式特性**

[slide]

# 思维转变——getChapterList

来自公司真实代码：`ChapterServiceImpl#getChapterList`

```java
public Map<String, Object> getChapterList(String userId) {
    Map<String, Object> result = new HashMap<>();
    List childChapter = new ArrayList();

    //获得孩子
    List<PStudentParent> children = chapterMapper.findChildrenByParentId(userId);

    for (PStudentParent child : children) {
        //准备孩子的信息
        Map<String,Object> childMap = getChildrenChapter(child);
        childChapter.add(childMap);
    }
    result.put("childrenlist", childChapter);

    return result;
}
```

*命令式编程*

[slide]

# 思维转变——getChapterList（Java 8）

```java
public Map<String, Object> getChapterList2(String userId) {
    List<PStudentParent> children = chapterMapper.findChildrenByParentId(userId);
    List<Map<String, Object>> childChapter = children.stream().map(this::getChildrenChapter).collect(Collectors.toList());
    return new HashMap<String, Object>(){{ put("childrenlist", childChapter); }};
}
```

*函数式，基于表达式的编程*

1. 消除不必要的临时变量；
2. 基于表达式编程，而非命令；
3. 合理运用Stream；
4. 集合库初始化。

[slide]

# 思维转变——获取当天的开始时间和结束时间（millis）

*Non Java 8*
```java
    public static DayStartAndEndEntity getDayTime() {
        DayStartAndEndEntity dayStartAndEndEntity = new DayStartAndEndEntity();
        long current = System.currentTimeMillis();
        long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
        dayStartAndEndEntity.setStartTime(zero);
        long twelve=zero+24*60*60*1000-1;//今天23点59分59秒的毫秒数
        dayStartAndEndEntity.setEndTime(twelve);

        return dayStartAndEndEntity;
    }
```

*Use Java 8 time*
```java
    public static DayStartAndEndEntity getDayTime() {
        LocalDateTime nowBegin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);   // 1
        long startMillis = nowBegin.toInstant(ZoneOffset.ofHours(8)).toEpochMilli(); // 2
        long endMillis = start + (60L * 60 * 24 * 1000 - 1);                         // 3
        return new DayStartAndEndEntity(start, end);
    }
```

1. 获取当天开始时间；
2. 将开始时间转换为 epoch millis；
3. 获取当天结束时间戳。

[slide]

# 思维转变——查询用户名

```java
public PageInfoBT commentMore(Page page, ActivityCommentMoreDTO activityCommentMoreDTO) {
    List<ReplyComment> list = activityCommentMapper.commentMore(page, activityCommentMoreDTO);
    list.stream().forEach(x -> {
        String commentatorId = x.getCommentatorId();
        String commenteeId = x.getCommenteeId();
        String commentatorName = userService.selectNameByChildId(commentatorId);
        String commenteeName = userService.selectNameByChildId(commenteeId);
        x.setCommentatorName(commentatorName);
        x.setCommenteeName(commenteeName);
    });
    return new PageInfoBT<>(page, list);
}
```

假设`list.size`为10，则`forEach`循环一共要执行20次SQL查询。

[slide]

# 思维转变——查询用户名

```java
public PageInfoBT commentMore2(Page page, ActivityCommentMoreDTO activityCommentMoreDTO) {
    List<ReplyComment> list = activityCommentMapper.commentMore(page, activityCommentMoreDTO);
    List<String> userIds = list.stream()
            .flatMap(item -> Stream.of(item.getCommentatorId(), item.getCommenteeId()))
            .collect(Collectors.toList());
    HashMap<String, String> userIdNameMap = userService.selectIdNameMapByUserIds(userIds);
    list.forEach(x -> {
        x.setCommentatorName(userIdNameMap.get(x.getCommentatorId()));
        x.setCommenteeName(userIdNameMap.get(x.getCommenteeId()));
    });
    return new PageInfoBT<>(page, list);
}
````

通过提前收集所有用户ID，将20次SQL查询降低为1次。

[slide]

# Lambda表达式

一种紧凑的、传递行为的方式。

*无参*
```java
Runnable noArguments = () -> System.out.println("Hello World");
```

*有参*
```java
ActionListener oneArgument = event -> System.out.println("button clicked");
```

*多行*
```java
Runnable multiStatement = () -> {
    System.out.print("Hello");
    System.out.println(" World");
};
```

*自动推导类型*
```java
BinaryOperator<Long> add = (x, y) -> x + y;
```

*显示声明类型*
```java
BinaryOperator<Long> addExplicit = (Long x, Long y) -> x + y;
```

[slide]

# Lambda表达式
## 引用值，而不是变量

*只能引用显示声明为`final`的变量*
```java
final String name = getUserName();
button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent event) {
        System.out.println("hi " + name);
    }
});
```

*可引用既成事实的`final`变量*
```java
String name = getUserName();
button.addActionListener(event -> System.out.println("hi " + name));
```

*未成既成事实的`final`变量无法编译通过*
```java
String name = getUserName();
name = formatUserName(name);
button.addActionListener(event -> System.out.println("hi " + name));
```

[slide]

# Lambda表达式
## 函数接口

函数接口是只有一个抽象方法的接口，用途Lambda表达式的类型。

函数式接口可以做到向下兼容性，Java已经有了很多单抽象方法的接口，这些接口在不作任务修改的情况下可用于Java 8的Lambda表达式。

Java中重要的函数接口

| 接口                | 参数     | 返回类型 | 示例 |
|---------------------|----------|---------|------|
| `Predicate<T>`      | `T`      | boolean | 是否匹配 |
| `Consumer<T>`       | `T`      | void    | 消费一个元素 |
| `Function<T, R>`    | `T`      | R       | 获得Persion对象的名字 |
| `Supplier<T>`       | `None`   | T       | 工厂方法，生成一个对象 |
| `UnaryOperator<T>`  | `T`      | T       | 逻辑非（`!`） |
| `BinaryOperator<T>` | `(T, T)` | T       | 求两个数的乘积（`*`） |

[slide]

# Stream

* 流使得程序员得以站在更高的抽象层次上对集合进行操作。
* Stream 是用函数式编程方式在集合类上进行复杂操作的工具。

*计算属于5.3班（5年级3班）的学生数量*
```java
int count = 0;
for (Student student : students) {
  if (student.isClass("5.3") {
    count += 1;
  }
}
```

*使用Java 8 Stream计算*
```java
long count = students.stream()
                     .filter(student -> student.isClass("5.3"))
                     .count();
```

[slide]

# Stream——常用流操作

* `collect(toList())`。求值动作，将Stream里的值生成一个列表。
* `map`。映射转换，通过函数（Lambda）将Stream里的值转换成另外一种类型。
* `filter`。过滤，只保留匹配为真的元素。
* `flatMap`。用Stream替换值，然后将多个Stream连接成一个Stream。
* `max`和`min`。在Stream上求最大值和最小值。
* `count`。计算Stream的数量。
* `reduce`。从Stream中生成一个值，
```java
int count = Stream.of(1, 2, 3).reduce(0, (acc, element) -> acc + element);
assertEquals(6, count);
```

[slide]

# Stream 示例

找到专辑里所有长度大于1分钟的曲目名称。
```java
public Set<String> findLongTracks(List<Album> albums) {
    Set<String> trackNames = new HashSet<>();
    for(Album album : albums) {
        for (Track track : album.getTrackList()) {
            if (track.getLength() > 60) {
                String name = track.getName();
                trackNames.add(name);
            }
        }
    }
    return trackNames;
}
```

**重构 1** *等价替换：`for` -> `forEach`*

```java
public Set<String> findLongTracks(List<Album> albums) {
    Set<String> trackNames = new HashSet<>();
    albums.stream()
          .forEach(album -> {
              album.getTracks()
                  .forEach(track -> {
                      if (track.getLength() > 60) {
                          String name = track.getName();
                          trackNames.add(name);
                      }
                  });
          });
    return trackNames;
}
```

[slide]

# Stream 示例

**重构 2** *内层`forEach` -> `Stream`*

```java
public Set<String> findLongTracks(List<Album> albums) {
    Set<String> trackNames = new HashSet<>();
    albums.stream()
          .forEach(album -> {
              album.getTrackList().stream()
                  .filter(track -> track.getLength() > 60)
                  .map(track -> track.getName())
                  .forEach(name -> trackNames.add(name));
          });
    return trackNames;
}
```

**重构 3** *消除嵌套的`Stream`，将`Stream[Stream[Track]]`拉平为`Stream[Track]`*

```java
public Set<String> findLongTracks(List<Album> albums) {
    Set<String> trackNames = new HashSet<>();
    albums.stream()
          .flatMap(album -> album.getTrackList().stream())
          .filter(track -> track.getLength() > 60)
          .map(track -> track.getName())
          .forEach(name -> trackNames.add(name));
    return trackNames;
}
```

`flatMap`将两层`Stream`拉平为一层`Stream`。

[slide]

# Stream 示例

**重构 4** *消除临时变量，完整`Stream`化*

```java
public Set<String> findLongTracks(List<Album> albums) {
    return albums.stream()
                 .flatMap(album -> album.getTrackList().stream())  // 1
                 .filter(track -> track.getLength() > 60)          // 2
                 .map(track -> track.getName())                    // 3
                 .collect(toSet());                                // 4
}
```

1. 将专辑里的所有曲目构造成一个Stream
2. 过滤出时长大于1分钟的曲目
3. 获取所有曲目的名称
4. 将流`Stream`收集成`Set`

[slide]

# 方法引用

标准语法为`Classname::methodName`，**后面不需要加小括号**。

```java
artists.map(artist -> artist.getName())
```

可转简写为方法引用：
```java
artists.map(Artist::getName)
```

```java
(name, nationality) -> new Artist(name, nationality)
```

可简写为方法引用：
```java
Artist::new
```

[slide]

# 高阶函数

高阶函数指：接受另一个函数作为参数，或返回一个函数的函数。

Stream接口中几乎所有函数都是高阶函数。

[slide]

# CompletableFuture

**定义一个异步函数**
```java
public CompletableFuture<Double> getPriceAsync(String product) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return random.nextDouble();
    });
}
```

典型的“混聚”式应用 

![典型的“混聚”式应用](/img/java8-block-async.svg)

[slide]

# 聚合操作——阻塞

假定每个`getTopic`和`getHotTopic`函数都需要花费1秒钟执行。

```java
public static void blockProcess() {
    Instant begin = Instant.now();
    String twitterTopic = getTopic("twitter");
    String facebookTopic = getTopic("facebook");
    String weiboTopic = getTopic("weibo");
    List<String> topics = Arrays.asList(twitterTopic, weiboTopic, facebookTopic);
    String hotTopic = getHotTopic(topics);
    Duration duration = Duration.between(begin, Instant.now());
    System.out.println("执行时间：" + duration + "。" + hotTopic);
}
```

*执行时间：PT4.001S。twitter*

[slide]

# 聚合操作——异步

```java
public static void asyncProcess() throws ExecutionException, InterruptedException {
    Instant begin = Instant.now();
    CompletableFuture<String> twitterFuture = getTopicAsync("twitter");
    CompletableFuture<String> facebookFuture = getTopicAsync("facebook");
    CompletableFuture<String> weiboFuture = getTopicAsync("weibo");
    CompletableFuture<List<String>> listFuture = sequence(Arrays.asList(twitterFuture, facebookFuture, weiboFuture));
    CompletableFuture<String> hotTopic = listFuture.thenCompose(sortedList -> topTopicAsync(sortedList));
    String frenchTopic = hotTopic.get();
    Duration duration = Duration.between(begin, Instant.now());
    System.out.println("执行时间：" + duration + "。" + frenchTopic);
}
```

*执行时间：PT2.072S。twitter*

[slide]

# CompletableFuture 组合

一个异步操作完成后再执行另一个异步操作，但整体非阻塞：

```java
CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 1);
CompletableFuture<Integer> result1 = future1.thenCompose(n -> CompletableFuture.supplyAsync(() -> n + 3));
```

同时执行两个异步操作，以非阻塞的方式将结果合并到一起：

```java
CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 1);
CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 2);
CompletableFuture<Integer> result2 = future1.thenCombine(future2, (x, y) -> x + y);
```

`List<CompletableFuture<T>>`以非阻塞的方式将每个异步操作结果聚合到一起，返回`CompletableFuture<List<T>>`：

```
public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
    CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
}
```

从3个异步操作里取最先返回那个的结果，其它忽略：

```
CompletableFuture<Object> result3 = CompletableFuture.anyOf(future1, future2, future3);
```

[slide]

# Scala的函数式特性

immutable数据类型

```sbtshell
scala> val a = 1
a: Int = 1

scala> a = 2
<console>:12: error: reassignment to val
       a = 2
         ^
```

[slide]

# 字符串插值

```sbtshell
scala> val name = "杨景"
name: String = 杨景

scala> val s1 = s"中少红卡（北京）教育科技有限公司码农：$name"
s1: String = 中少红卡（北京）教育科技有限公司码农：杨景

scala> val s2 = s"当前时间：${LocalDateTime.now()}"
s2: String = 当前时间：2019-01-16T23:58:07.064
```

[slide]

# 表达式

所有语句都是表达式

*if表达式有值*
```sbtshell
scala> val b = if (true) "真" else "假"
b: String = 真
```

*for comprehension（for推导值）有值*
```sbtshell
scala> val oddX2 = for (item <- list if item % 2 == 1) yield item * 2
oddX2: scala.collection.immutable.IndexedSeq[Int] = Vector(2, 6, 10, 14, 18)
```

[slide]

# 模式匹配

**Java**

```java
public void receive(message: Object) {
    if (message isInstanceOf String) {
        String strMsg = (String) message;
        ....
    } else if (message isInstanceOf java.util.Date) {
        java.util.Date dateMsg = (java.util.Date) message;
        ....
    } ....
}
```

**Scala**

```scala
def receive(message: Object): Unit = {
  message match {
    case str: String          => println("message is String") 
    case date: java.util.Date => println("message is Date")
    case _                    => println("message is other type")
  }
}
```

[slide]

# 函数

```sbtshell
scala> def calc(n1: Int, n2: Int): (Int, Int) = {
     |   (n1 + n2, n1 * n2)
     | }
calc: (n1: Int, n2: Int)(Int, Int)

scala> val (add, sub) = calc(5, 1)
add: Int = 6
sub: Int = 5
```

```sbtshell
scala> val calcVar = calc _
calcVar: (Int, Int) => (Int, Int) = <function2>

scala> calcVar(2, 3)
res4: (Int, Int) = (5,6)

scala> val sum: (Int, Int) => Int = (x, y) => x + y
sum: (Int, Int) => Int = <function2>

scala> sum(5, 7)
res5: Int = 12
```

[slide]

# 异步、并发

```scala
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
val futures = (0 until 10).map { i =>
  Future {
    val s = i.toString
    print(s)
    s
  }
}
val future = Future.reduce(futures)((x, y) => x + y)
val result = Await.result(future, Duration.Inf) // result is 0123456789
```

[slide]

# 练习

## 题目

```scala
def strtr(src:String, from:String, to:String): String
```

from 和 to是等长的字符串, 要求将src中值为from(i)的字符转换成to(i)

例如: `strtr("abcdaf", "ac", "AC") == "AbCdAf"`

[slide]

## 练习——Java版

```java
public static String strtr(String src, String from, String to) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < src.length(); i++) {
        char c = src.charAt(i);
        char cc = c;
        for (int j = 0; j < from.length(); j++) {
            if (from.charAt(j) == c) {
                cc = to.charAt(j);
                break;
            }
        }
        buffer.append(cc);
    }
    return buffer.toString();
}
```

## 练习——Scala版

```scala
def strtr(src: String, from: String, to: String): String = {
  val mat = (from zip to).toMap
  src.map(s => mat.getOrElse(s, s))
}
```

[slide]

# Answer

<p style="text-align:right">杨景</p>
<p style="text-align:right">www.yangbajing.me</p>
<p style="text-align:right">weibo.com/yangbajing</p>
<p style="text-align:right">yangbajing at gmail com</p>
