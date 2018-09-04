title: 使用PostgreSQL
speaker: 杨景（羊八井）
url: https://www.yangbajing.me/shares/publish/postgres-10.html
files: /css/moon.css,/img

[slide]

# 使用PostgreSQL
## 分享人：杨景

[slide]

# PostgreSQL特性

- **表分区**
- **逻辑复制**
- **并行查询**
- **查询JIT编译**
- **丰富的数据类型**：数组、JSON、XML、Hstore、network address、point、geometric等
- **用户自定义数据类型**
- **丰富的数据索引类型**
- **Common Table Expressions**：通用表表达式和递归
- **物化视图**：物化视图就像普通视图那样代表一个经常使用的查询结果集，只是结果集像一个普通表那样存储在磁盘上。物化视图也可以添加索引，不像普通视图每次请求时重新生成，物化视图是及时的快照。
- **FDW**：Forerign Data Wrappers，外部数据包装。可通过PostgreSQL访问其它关系型数据库，如：MySQL、Oracle、MS SQL Server等（也可以访问其它Postgres数据库实例。同时，还能访问NoSQL和文件存储，如：HBase、Cassandra、MongoDB、Redis、CSV/Text、JSON等
- ……

[slide]

# 数值类型

| 名字      | 存储尺寸     | 描述     |  范围   |
|----------|--------------|---------|---------|
| smallint | 2字节        |小范围整数	| -32768 to +32767 |
| integer  | 4字节        |整数的典型选择 | -2147483648 to +2147483647 |
| bigint   | 8字节        |大范围整数 | -9223372036854775808 to +9223372036854775807 |
| numeric  | 可变         |用户指定精度，精确 | 最高小数点前131072位，以及小数点后16383位 |
| real, float4 | 4字节        |可变精度，不精确 | 6位十进制精度 |
| double precision, float8 | 8字节|可变精度，不精确 | 15位十进制精度 |
| serial   | 4字节        | 自动增加的整数  | 1到2147483647 |
| bigserial| 8字节        | 自动增长的大整数| 1到9223372036854775807 |

[slide]

# 字符/二进制类型

| 名字      | 存储尺寸     | 描述     |
|----------|--------------|---------|
| varchar(n) |            | 有限制的变长 |
| char(n)  |             | 定长 |
| text  |                | 无限变长 |
| bytea    | 1或4字节外加真正的二进制串 | 变长二进制串 |

[slide]

# 日期、时间类型

| 名字                                | 存储尺寸| 描述                   | 最小值 | 最大值     |解析度 |
|-------------------------------------|--------|-----------------------|--------|-----------|-------|
| timestamp [(p)] [without time zone] | 8字节  | 包括日期和时间（无时区） | 4713 BC| 294276 AD |1微秒/14位 |
| timestamp [(p)] with time zone      | 8字节  | 包括日期和时间，有时区   | 4713 BC| 294276 AD |1微秒/14位 |
| date                                | 4字节  | 日期（没有一天中的时间） | 4713 BC	|5874897 AD |1日 |
| time [(p)] [without time zone ]     | 8字节  | 一天中的时间（无日期）   | 00:00:00|24:00:00  |1微秒/14位 |
| time [(p)] with time zone           | 12字节 | 一天中的时间（不带日期），带有时区 | 00:00:00+1459 |24:00:00-1459|1微秒/14位 |
| interval [fields] [(p)]             | 16字节 | 时间间隔	               | -178000000年|178000000年 |1微秒/14位 |

[slide]

# JSON类型

- JSON 基本类型和相应的PostgreSQL类型

|JSON   |基本类型 |PostgreSQL类型	注释 |
|-------|--------|------------------|
|string |text    |不允许\u0000，如果数据库编码不是 UTF8，非 ASCII Unicode 转义也是这样 |
|number |numeric |不允许NaN 和 infinity值 |
|boolean|boolean |只接受小写true和false拼写 |
|null   |(无)    |SQL NULL是一个不同的概念 |

- *示例*

```sql
select '[1, 2, "foo", null]'::jsonb;
select '{"foo": [true, "bar"], "tags": {"a": 1, "b": null}}'::jsonb;
```

[slide]

# JSON类型

## jsonb与json

- 当一个 json 值被输入并且接着不做任何附加处理就输出时，json会输出和输入完全相同的文本，而jsonb则不会保留语义上没有意义的细节（例如空格）。例如，注意下面的不同：

```sql
SELECT '{"bar":    "   baz", "balance":    7.77,     "active":false}'::json;
-------------------------------------------------
{"bar":    "   baz", "balance":    7.77,     "active":false}
(1 row)

SELECT '{"bar":    "   baz", "balance":    7.77,     "active":false}'::jsonb;
--------------------------------------------------
{"bar": "   baz", "active": false, "balance": 7.77}
(1 row)
```

- 值得一提的一种语义上无意义的细节是，在jsonb中数据会被按照底层 numeric类型的行为来打印。实际上，这意味着用E记号 输入的数字被打印出来时就不会有该记号，例如：

```sql
SELECT '{"reading": 1.230e-5}'::json, '{"reading": 1.230e-5}'::jsonb;
         json          |          jsonb
-----------------------+-------------------------
 {"reading": 1.230e-5} | {"reading": 0.00001230}
(1 row)
```

[slide]

# XML类型

- 通过xmlparse函数从字符数据中生成一个xml类型

```sql
select XMLPARSE (DOCUMENT '<?xml version="1.0"?><book><title>Manual</title><chapter>...</chapter></book>');
select XMLPARSE (CONTENT 'abc<foo>bar</foo><bar>foo</bar>');
select '<foo>bar</foo>'::xml;
```

[slide]

# 其它类型

| 名字      | 存储尺寸     | 描述     |  范围   |
|----------|--------------|---------|---------|
| money    | 8字节        | 货币额  | -92233720368547758.08到+92233720368547758.07 |
| bool     | 1字节        | 状态为真或假 |      |
| enum     |             | 通过 create type mood as enum('sad', 'ok', 'happy'); 创建枚举类型 |      |
| cidr     | 7或19字节   | IPv4和IPv6网络 | |
| inet     | 7或19字节   | IPv4和IPv6主机以及网络 | |
| macaddr  | 6字节       | MAC地址 | |
| macaddr8 | 8字节       | MAC 地址 (EUI-64 格式) | |

[slide]

# PostgreSQL安装

- Docker
- 二进制安装包：Linux、Windows、Mac
- Linux软件仓库：Yum/dnf、Apt
- 第三方软件包：BigSQL（https://www.openscg.com/）
- 源码编译

[slide]

# 使用Docker安装PostgreSQL

## Dockerfile 

- Docker脚本，设置系统默认字符集为`zh_CN.UTF-8`

```dockerfile
FROM postgres:10.4

RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8

ENV LANG zh_CN.utf8
ENV TZ Asia/Shanghai

COPY init.sql /docker-entrypoint-initdb.d/
```

- 使用Docker在开发环境运行Postgres是一个很好的方案，可以统一团队成员的数据库版本及运行环境。

[slide]

# 使用Docker安装PostgreSQL

## init.sql

- 数据库初始化脚本，`docker run`时执行些数据库初始化工作。

```sql
create user massdata with nosuperuser replication encrypted password 'Massdata.2018';
create database massdata owner = massdata template = template0 encoding = 'UTF-8' 
  lc_ctype = 'zh_CN.UTF-8' lc_collate = 'zh_CN.UTF-8';
\c massdata;
create extension adminpack;
create extension hstore;
```

[slide]

# 使用Docker安装PostgreSQL

## Build & Run

```sh
$ docker build -t postgres-10-dev .
$ docker run -h postgres-10-dev -p 5432:5432 --name=postgres-10-dev -d postgres-10-dev
```

## 登录数据库

```sh
$ docker run -it --rm --link=postgres-10-dev postgres-10-dev \
  psql -h postgres-10-dev -d massdata -U massdata
psql (10.4 (Debian 10.4-2.pgdg90+1))
Type "help" for help.

massdata=> 
```

[slide]

# DDL

- 创建表

```sql
create table t_book(
  id serial primary key,
  isbn varchar(64) not null,
  title varchar(255) not null,
  authors text[] not null,
  created_at timestamptz
);
```

- 创建索引

```sql
create unique index t_book_isbn_idx on t_book(title);
```

- 添加列

```sql
alter table t_book add column description text null;
```

- 查看表定义：`\d t_book;`

```sql
massdata=> \d t_book;
                                       Table "public.t_book"
   Column    |           Type           | Collation | Nullable |              Default               
-------------+--------------------------+-----------+----------+------------------------------------
 id          | integer                  |           | not null | nextval('t_book_id_seq'::regclass)
 isbn        | character varying(64)    |           | not null | 
 title       | character varying(255)   |           | not null | 
 authors     | text[]                   |           | not null | 
 created_at  | timestamp with time zone |           |          | 
 description | text                     |           |          | 
Indexes:
    "t_book_pkey" PRIMARY KEY, btree (id)
    "t_book_isbn_idx" UNIQUE, btree (title)
```

[slide]

# 查询

```sql
massdata=> select id, isbn, title, authors from t_book;
 id |       isbn        |             title              |                    authors                    
----+-------------------+--------------------------------+-----------------------------------------------
  1 | 978-7-115-48356-0 | Scala实用指南                  | {文卡特·苏帕拉马尼亚姆}
  2 | 9787111370048     | Java并发编程                   | {"Brian Goetz","Time Peierls","Joshua Bloch"}
  3 | 9787111255833     | Effective Java中文版（第二版） | {"joshua Bloch"}
(3 rows)
```

[slide]

# 数组字段的查询

- 查找作者 **Joshua Bloch** 写的所有书籍。

```sql
massdata=> select id, isbn, title, authors from t_book
massdata-> where 'Joshua Bloch' like any(authors);
 id |     isbn      |             title              |     authors      
----+---------------+--------------------------------+------------------
  3 | 9787111255833 | Effective Java中文版（第二版） | {"joshua Bloch"}
(1 row)
```

- 通过`any`函数来匹配数组内的任一个元素。
- *因为大小写问题，作者 **Joshua Bloch** 的书只找到一本。*

[slide]

# 数组字段的查询

- 通过 `unnest` 函数将数组字段值转换成表数据记录

```sql
massdata=> select p.id, p.isbn, p.title, p.authors from
massdata-> (select *, unnest(authors) as author from t_book) p
massdata-> where p.author like '%Bloch';
 id |     isbn      |             title              |                    authors                    
----+---------------+--------------------------------+-----------------------------------------------
  2 | 9787111370048 | Java并发编程                   | {"Brian Goetz","Time Peierls","Joshua Bloch"}
  3 | 9787111255833 | Effective Java中文版（第二版） | {"joshua Bloch"}
(2 rows)
```

- 通过 `ilike` 操作符来进行忽略大小写的字符比较

```sql
massdata=> select id, isbn, title, authors from t_book
massdata-> where 'joshua Bloch' ilike any(authors);
 id |     isbn      |             title              |                    authors                    
----+---------------+--------------------------------+-----------------------------------------------
  2 | 9787111370048 | Java并发编程                   | {"Brian Goetz","Time Peierls","Joshua Bloch"}
  3 | 9787111255833 | Effective Java中文版（第二版） | {"joshua Bloch"}
(2 rows)
```

[slide]

# JSON

- 创建 `t_user` 用户表

```sql
create table t_user(
  id bigserial primary key,
  username varchar(255) not null,
  attrs jsonb not null default '{}',
  created_at timestamptz not null default now()
);
create unique index t_user_username_uidx on t_user(username);
```

- JSON字段的插入

```sql
insert into t_user(username, attrs) values
('yangbajing', '{"nickname":"羊八井","age":32,"email":"yangbajing@gmail.com", "contacts":[{"city":"重庆","address":"渝北区金开大道西段106号10栋移动新媒体产业大厦11楼"}]}'),
('yangjing', '{"nickname":"杨景","age":32}'),
('yangjiajiang', '{"nickname":"杨家将","age":32, "contacts":[{"city":"江津","address":"重庆市江津区南门路"}]}');
```

- 查询

```sql
massdata=> select * from t_user;
 id |  username  |                                                                               attrs                                                                                |          created_at

----+------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------
-------
  2 | yangjing   | {"age": 32, "nickname": "杨景"}                                                                                                                                    | 2018-09-03 12:00:42.274
754+00
  1 | yangbajing | {"age": 33, "email": "yangbajing@gmail.com", "contacts": [{"city": "重庆", "address": "渝北区金开大道西段106号10栋移动新媒体产业大厦11楼"}], "nickname": "羊八井"} | 2018-09-03 12:00:42.274
754+00
  3 | yangjiajiang | {"age": 32, "contacts": [{"city": "江津", "address": "重庆市江津区南门路"}], "nickname": "杨家将"}                                                                 | 2018-09-04 08:53:35.6
29468+00
(2 rows)
```

[slide]

# JSON

- 动态schema
- 不用再定义 `col1`，`col2`，`col3` 这样无意义的字段

### 通过 `->>` 操作符根据 **username** 查询

```sql
massdata=> select id, username, attrs from t_user where attrs->>'nickname' = '杨景';
 id | username |              attrs              
----+----------+---------------------------------
  2 | yangjing | {"age": 32, "nickname": "杨景"}
(1 row)
```

### 根据jsonb字段进行检索

```sql
massdata=> select id, username, attrs from t_user where attrs @> '{"contacts":[{"city":"重庆"}]}'::jsonb;
 id |  username  |                                                                               attrs
  1 | yangbajing | {"age": 32, "email": "yangbajing@gmail.com", "contacts": [{"city": "重庆", "address": "渝北区金开大道西段106号10栋移动新媒体产业大厦11楼"}], "nickname": "羊八井"}
(1 row)

massdata=> select id, username, attrs from t_user where attrs @> '{"contacts":[{"city":"江津"}]}'::jsonb;
 id |   username   |                                               attrs
  3 | yangjiajiang | {"age": 32, "contacts": [{"city": "江津", "address": "重庆市江津区南门路"}], "nickname": "杨家将"}
(1 row)

massdata=> select id, username, attrs from t_user where attrs->'age' = '32'::jsonb;
 id |   username   |                                               attrs
  2 | yangjing     | {"age": 32, "nickname": "杨景"}
  3 | yangjiajiang | {"age": 32, "contacts": [{"city": "江津", "address": "重庆市江津区南门路"}], "nickname": "杨家将"}
```

[slide]

# JSON

## jsonb索引

- GIN 索引可以被用来有效地搜索在大量jsonb文档（数据）中出现 的键或者键值对。提供了两种 GIN “操作符类”，它们在性能和灵活 性方面做出了不同的平衡。

```sql
create index t_user_attrs_idx ON t_user using gin(attrs);
```

- jsonb操作符

|操作符|右操作数类型|描述                                      |例子                                              |例子结果    |
|-----|-----------|------------------------------------------|-------------------------------------------------|------------|
|->   |int        |获得JSON 数组元素（索引从 0 开始，负整数结束）|'[{"a":"foo"},{"b":"bar"},{"c":"baz"}]'::json->2 |{"c":"baz"} |
|->   |text       |通过键获得JSON 对象域                       |	'{"a": {"b":"foo"}}'::json->'a'                |{"b":"foo"} |
|->>  |int        |以文本形式获得JSON 数组元素                 |	'[1,2,3]'::json->>2                            |3           |
|->>  |text       |以文本形式获得JSON 对象域                   |	'{"a":1,"b":2}'::json->>'b'                    |2           |
|#>   |text[]     |获取在指定路径的JSON 对象	                 |'{"a": {"b":{"c": "foo"}}}'::json#>'{a,b}'       |{"c": "foo"}|
|#>>  |text[]     |以文本形式获取在指定路径的JSON 对象          |	'{"a":[1,2,3],"b":[4,5,6]}'::json#>>'{a,2}'    |3           |

[slide]

# JSON

## 更多jsonb操作符

<table border="1">
    <thead>
    <tr>
        <th>操作符</th>
        <th>右操作数类型</th>
        <th>描述</th>
        <th>例子</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><code class="literal">@&gt;</code></td>
        <td><code class="type">jsonb</code></td>
        <td>左边的 JSON 值是否包含顶层右边JSON路径/值项?</td>
        <td><code class="literal">'{"a":1, "b":2}'::jsonb @&gt; '{"b":2}'::jsonb</code></td>
    </tr>
    <tr>
        <td><code class="literal">&lt;@</code></td>
        <td><code class="type">jsonb</code></td>
        <td>左边的JSON路径/值是否包含在顶层右边JSON值中？</td>
        <td><code class="literal">'{"b":2}'::jsonb &lt;@ '{"a":1, "b":2}'::jsonb</code></td>
    </tr>
    <tr>
        <td><code class="literal">?</code></td>
        <td><code class="type">text</code></td>
        <td><span class="emphasis"><em>字符串</em></span>是否作为顶层键值存在于JSON值中？</td>
        <td><code class="literal">'{"a":1, "b":2}'::jsonb ? 'b'</code></td>
    </tr>
    <tr>
        <td><code class="literal">?|</code></td>
        <td><code class="type">text[]</code></td>
        <td>这些数组<span class="emphasis"><em>字符串</em></span>中的任何一个是否作为顶层键值存在？</td>
        <td><code class="literal">'{"a":1, "b":2, "c":3}'::jsonb ?| array['b', 'c']</code></td>
    </tr>
    <tr>
        <td><code class="literal">?&amp;</code></td>
        <td><code class="type">text[]</code></td>
        <td>这些数组<span class="emphasis"><em>字符串</em></span>是否作为顶层键值存在？</td>
        <td><code class="literal">'["a", "b"]'::jsonb ?&amp; array['a', 'b']</code></td>
    </tr>
    <tr>
        <td><code class="literal">||</code></td>
        <td><code class="type">jsonb</code></td>
        <td>连接两个<code class="type">jsonb</code>值到新的<code class="type">jsonb</code>值</td>
        <td><code class="literal">'["a", "b"]'::jsonb || '["c", "d"]'::jsonb</code></td>
    </tr>
    <tr>
        <td><code class="literal">-</code></td>
        <td><code class="type">text</code></td>
        <td>从左操作数中删除键/值对或<span class="emphasis"><em>字符串</em></span>元素。基于键值匹配键/值对。</td>
        <td><code class="literal">'{"a": "b"}'::jsonb - 'a' </code></td>
    </tr>
    <tr>
        <td><code class="literal">-</code></td>
        <td><code class="type">text[]</code></td>
        <td>从左操作数中删除多个键/值对或<span class="emphasis"><em>string</em></span>元素。
            键/值对基于其键值进行匹配。
        </td>
        <td><code class="literal">'{"a": "b", "c": "d"}'::jsonb - '{a,c}'::text[] </code></td>
    </tr>
    <tr>
        <td><code class="literal">-</code></td>
        <td><code class="type">integer</code></td>
        <td>删除指定索引的数组元素（负整数结尾）。如果顶层容器不是一个数组，那么抛出错误。</td>
        <td><code class="literal">'["a", "b"]'::jsonb - 1 </code></td>
    </tr>
    <tr>
        <td><code class="literal">#-</code></td>
        <td><code class="type">text[]</code></td>
        <td>删除指定路径的域或元素（JSON数组，负整数结尾）</td>
        <td><code class="literal">'["a", {"b":1}]'::jsonb #- '{1,b}'</code></td>
    </tr>
    </tbody>
</table>

[slide]

# hstore

- hstore是Postgres的一个扩展，数据类型用来在一个单一PostgreSQL值中存储键值对。这在很多情景下都有用，例如带有很多很少被检查的属性的行或者半结构化数据。键和值都是简单的文本字符串。
- hstore的文本表示由零个或多个英文逗号来分隔`key=>value`对：

```sql
massdata=> select 'a=>1,a=>2,b=>null'::hstore;
       hstore
---------------------
 "a"=>"1", "b"=>NULL
(1 row)
```

- hstore里key是唯一的，键值对之间或者`=>`号周围的空白将被忽略。一个值可以是SQL `NULL`（不区分大小写）。

## jsonb和hstore？

- 更推荐使用jsonb类型，JSON可表达的数据类型更丰富，可嵌套。

[slide]

# WITH查询（CTE，公共表表达式）

- WITH提供了一种方式来书写在一个大型查询中使用的辅助语句。这些语句通常被称为公共表表达式或CTE，它们可以被看成是定义只在一个查询中存在的临时表。在WITH子句中的每一个辅助语句可以是一个SELECT、INSERT、UPDATE或DELETE，并且WITH子句本身也可以被附加到一个主语句，主语句也可以是SELECT、INSERT、UPDATE或DELETE。

```sql
WITH regional_sales AS (
        SELECT region, SUM(amount) AS total_sales
        FROM orders
        GROUP BY region
     ), top_regions AS (
        SELECT region
        FROM regional_sales
        WHERE total_sales > (SELECT SUM(total_sales)/10 FROM regional_sales)
     )
SELECT region,
       product,
       SUM(quantity) AS product_units,
       SUM(amount) AS product_sales
FROM orders
WHERE region IN (SELECT region FROM top_regions)
GROUP BY region, product;
```

[slide]

# WITH查询（CTE，公共表表达式）

## RECURSIVE 递归

- 计算1到100的整数和

```sql
massdata=> WITH RECURSIVE t(n) AS (
    VALUES (1)
    UNION ALL
    SELECT n+1 FROM t WHERE n < 100
    )
massdata-> SELECT sum(n) FROM t;
 sum
------
 5050
(1 row)
```

[slide]

# WITH查询（CTE，公共表表达式）

## 找到某个组织的所有父组织

```sql
massdata=> select * from t_org;
 id |      name      | parent
----+----------------+--------
  1 | 机级组织       |
  2 | 市工商         |      1
  3 | 市公安         |      1
  4 | 市工商小微企部 |      2
(4 rows)
```

```sql
massdata=> with recursive tree as (
        select id, name, parent from t_org where id = 4
            union all
        select t_org.id, t_org.name, t_org.parent from t_org, tree where t_org.id = tree.parent
        ) select id, name from tree offset 1;
 id |   name
----+----------
  2 | 市工商
  1 | 机级组织
(2 rows)
```

[slide]

# WITH查询（CTE，公共表表达式）

## 将父组织转换成数组

```sql
create or replace function array_reverse(anyarray) returns anyarray as $$
select array(select $1 [ i ] from generate_subscripts($1, 1) as s(i) order by i desc);
$$ language 'sql' strict immutable;
```

```sql
massdata=> select array_reverse(array(with recursive tree as (
        select id, name, parent from t_org where id = 4
            union all
            select t_org.id, t_org.name, t_org.parent from t_org, tree where t_org.id = tree.parent
        ) select id from tree offset 1)) as parents;
 parents
---------
 {1,2}
(1 row)

```

[slide]

# 从修改的行中返回数据

- `INSERT`、`UPDATE`和`DELETE`命令都有支持可选的`RETURNING`子句，使用`RETURNING`可以避免执行额外的数据查询来收集数据。
- 所允许的`RETURNING`子句的内容与`SELECT`命令的输出列表相同。它可以包含命令的目标表的列名，或者包含使用这些列的值表达式。一个常见的简写是`RETURNING *`，它按顺序选择目标表的所有列。
- 以下语句允许我们获得插入的自增ID值，获取更新后的数据和获取已删除的数据：

```sql
insert into t_org (name, parent) values ('市公安', 1) returning id;
update t_org set name = '市公安局' where id = 3 returing *;
delete from t_org where id = 3 returing *;
```

[slide]

# ON CONFLICT

- 使用`on conflict`可以在发生主键冲突时控制接下来的动作。我们可以选择更新已有数据，或者忽略本次操作。

```sql
massdata=> insert into t_org (id, name, parent) values (3, '市公安局', 1);
ERROR:  duplicate key value violates unique constraint "t_org_pkey"
DETAIL:  Key (id)=(3) already exists.
```

## insertOrUpdate

```sql
massdata=> insert into t_org (id, name, parent)
        values (3, '市公安局', 1)
        on conflict (id) do update set name = EXCLUDED.name, parent = EXCLUDED.parent;
INSERT 0 1
```

- 使用`on conflict(xxx) do`特性，必需指定（复合）主键。
- 可以使用`on conflict(xxx) do nothing`在主键冲突时进行忽略（不返回错误也不更新数据）。
- 批量插入语句也可以使用`on conflict(xxx) do`特性。

```sql
insert into t_org (id, name, parent)
values (3, '市公安局', 1), (4, '市工商局小微企部', 2), (5, '市体育局', 1)
on conflict (id) do update set name = EXCLUDED.name, parent = EXCLUDED.parent;
```

[slide]

# FDW

## 访问其它PostgreSQL数据库

1. 使用`CREATE EXTENSION`来安装**postgres_fdw**扩展。
0. 使用`CREATE SERVER`创建一个外部服务器对象，它用来表示你想连接的每一个远程数据库。指定除了`user`和`password`之外的连接信息作为该服务器对象的选项。
0. 使用`CREATE USER MAPPING`创建一个用户映射，每一个用户映射都代表你想允许一个数据库用户访问一个外部服务器。指定远程用户名和口令作为用户映射的`user`和`password`选项。
0. 为每一个你想访问的远程表使用`CREATE FOREIGN TABLE`或者`IMPORT FOREIGN SCHEMA`创建一个外部表。外部表的列必须匹配被引用的远程表。但是，如果你在外部表对象的选项中指定了正确的远程名称，你可以使用不同于远程表的表名和/或列名。

## 启动第二个PostgreSQL数据库

```bash
$ docker run -h postgres-10-dev2 --link=postgres-10-dev --name=postgres-10-dev2 -d postgres-10-dev
$ docker run -it --rm --link=postgres-10-dev2 postgres-10-dev psql -h postgres-10-dev2 -U postgres -d massdata
```

[slide]

# FDW

## 访问其它PostgreSQL数据库

```sql
massdata=# create extension postgres_fdw ;
CREATE EXTENSION
massdata=# create server foreign_server foreign data wrapper postgres_fdw options (host 'postgres-10-dev', port '5432', dbname 'massdata');
CREATE SERVER
massdata=#  create user MAPPING FOR postgres server foreign_server options (user 'postgres', password 'postgres');
CREATE USER MAPPING
massdata=# create foreign table ft_org(
          id     int          not null,
          name   varchar(128) not null,
          parent int          null
        ) server foreign_server options(schema_name 'public', table_name 't_org');
CREATE FOREIGN TABLE
massdata=# select * from ft_org ;
 id |      name      | parent
----+----------------+--------
  1 | 市级组织        |
  2 | 市工商          |      1
  3 | 市公安          |      1
  4 | 市工商小微企部   |      2
(4 rows)
```

[slide]

# FDW

## 访问其它PostgreSQL数据库

```sql
massdata=# \d ft_org
                         Foreign table "public.ft_org"
 Column |          Type          | Collation | Nullable | Default | FDW options
--------+------------------------+-----------+----------+---------+-------------
 id     | integer                |           | not null |         |
 name   | character varying(128) |           | not null |         |
 parent | integer                |           |          |         |
Server: foreign_server
FDW options: (schema_name 'public', table_name 't_org')
```

- **postgres_fdw**覆盖了较老的dblink模块，提供了更透明且兼容标准的语法来访问远程表，并拥有更好的性能。
- 可以使用`select`、`insert`、`update`和`delete`操作远程表。
- `insert`语句支持`on conflict do nothing`，但不支持`on conflict do update`。
- 建议外部表的列被声明为与被无用的远程表列完全相同的数据类型和排序规则（如果需要）。
- 与远程表的列匹配是通过名字而不是位置进行的。
- 更多内容：[http://www.postgres.cn/docs/10/postgres-fdw.html](http://www.postgres.cn/docs/10/postgres-fdw.html)

[slide]

# FDW

## 访问MySQL数据库

- [PostgreSQL foregin data wrapper for MySQL](https://github.com/EnterpriseDB/mysql_fdw)

## MySQL Dockerfile

```dockerfile
FROM mysql:5.7

ENV LANG zh_CN.utf8
ENV TZ Asia/Shanghai

COPY init.sql /docker-entrypoint-initdb.d/
```

```bash
$ docker build -t mysql-5.7-dev .
$ docker run -p 3306:3306 --name=mysql-5.7-dev -h mysql-5.7-dev -e MYSQL_ROOT_PASSWORD=Mysql.2018 -d mysql-5.7-dev
$ docker run --rm -it --link=mysql-5.7-dev mysql-5.7-dev mysql -h mysql-5.7-dev -D massdata -u massdata -pMassdata.2018
```

```sql
mysql> insert into test(name, created_at) values ('MySQL', now()), ('PostgreSQL', now());
Query OK, 2 rows affected (0.06 sec)
Records: 2  Duplicates: 0  Warnings: 0
```

[slide]

# FDW

## 访问MySQL数据库

- 启动一个支持`mysql_fdw`扩展的PostgreSQL数据库

```bash
$ docker run --name=pgsql-fdq-mysql -h pgsql-fdq-mysql --link=mysql-5.7-dev -d geographica/postgis-fdw-mysql
$ docker run -it --rm --link=pgsql-fdq-mysql geographica/postgis-fdw-mysql psql -h pgsql-fdq-mysql -U postgres  # 密码：postgres
```

```sql
postgres@pgsql-fdq-mysql ~> create extension mysql_fdw;
postgres@pgsql-fdq-mysql ~> create server mysql_server foreign data wrapper mysql_fdw options(host 'mysql-5.7-dev', port '3306');
postgres@pgsql-fdq-mysql ~> create user mapping for postgres server mysql_server options(username 'massdata', password 'Massdata.2018');
postgres@pgsql-fdq-mysql ~> create foreign table ft_test(
          id bigint not null,
          name       varchar(255),
          created_at timestamp
        ) server mysql_server options(dbname 'massdata', table_name 'test');
postgres@pgsql-fdq-mysql ~> select * from ft_test;
 id |    name    |     created_at
----+------------+---------------------
  1 | MySQL      | 2018-09-05 02:07:17
  2 | PostgreSQL | 2018-09-05 02:07:17
(2 rows)
```

[slide]

# FDW

## 访问MySQL数据库

- `mysql_fdw`支持PostgreSQL 9.3, 9.4, 9.5, 9.6, 10。
- 支持连接到MySQL、MariaDB
- **支持读写**
- **连接池**：使用连接池优化与MySQL数据库的连接。
- **where子句下推**：支持将where子句下推到外部服务器上执行，只传递需要的数据行到PostgreSQL。
- **column下推**：支持在外部服务器筛选需要的列后再将数据传递到PostgreSQL。
- **预编译语句**

[slide]

# 表分区

- 表分区是将逻辑上的一个大表分成一些小的物理上的片，表分区有很多优势：

1. 在某些情况下可显著提升查询性能。
0. 很少使用的数据可以被迁移到便宜且较慢的存储介质上。

- PostgreSQL内置支持两种分区形式：

1. **范围分区**：该表被分区到由键列或列集定义的“范围”中，分配给不同分区的值范围之间没有重叠。例如，可以按日期范围进行分区，也可以按特定业务对象的标识符范围进行分区。
0. **列表分区**：表通过明确列出每个分区中出现的键值进行分区。

[slide]

# Backup

TODO

[slide]

# Recovery

TODO

[slide]

# JDBC

TODO


