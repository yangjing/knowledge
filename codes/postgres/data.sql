----------------------------------------------------------------
-- #t_book
----------------------------------------------------------------
create table t_book(
  id serial primary key,
  isbn varchar(64) not null,
  title varchar(255) not null,
  authors text[] not null,
  created_at timestamptz not null default now()
);
create unique index t_book_isbn_uidx on t_book(title);
alter table t_book add column description text null;

insert into t_book(isbn, title, authors, description) values
('978-7-115-48356-0', 'Scala实用指南', '{"文卡特·苏帕拉马尼亚姆"}', '本书是为想要快速学习或者正在学习Scala编程语言的Java开发者写的，循序渐进地介绍了Scala编程语言的多个方面。'),
('9787111370048', 'Java并发编程', '{"Brian Goetz", "Time Peierls", "Joshua Bloch"}', '《Java并发编程实战》深入浅出地介绍了Java线程和并发,是一本完美的Java并发参考手册。'),
('9787111255833', 'Effective Java中文版（第二版）', '{"joshua Bloch"}', '在Java编程中78条极具实用价值的经验规则，这些经验规则涵盖了大多数开发人员每天所面临的问题的解决方案。');
----------------------------------------------------------------
-- #t_book
----------------------------------------------------------------

----------------------------------------------------------------
-- #t_user
----------------------------------------------------------------
create table t_user(
  id bigserial primary key,
  username varchar(255) not null,
  attrs jsonb not null default '{}',
  created_at timestamptz not null default now()
);
create unique index t_user_username_uidx on t_user(username);

insert into t_user(username, attrs) values
('yangbajing', '{"nickname":"羊八井","age":33,"email":"yangbajing@gmail.com", "contacts":[{"city":"重庆","address":"渝北区金开大道西段106号10栋移动新媒体产业大厦11楼"}]}'),
('yangjing', '{"nickname":"杨景","age":32}'),
('yangjiajiang', '{"nickname":"杨家将","age":32, "contacts":[{"city":"江津","address":"重庆市江津区南门路"}]}');
----------------------------------------------------------------
-- #t_user
----------------------------------------------------------------

----------------------------------------------------------------
-- #t_org
----------------------------------------------------------------
create table t_org(
  id            serial              primary key ,
  name          varchar(128)        not null,
  parent        int                 null
);
insert into t_org(name, parent) values
('市级组织', null),
('市工商', 1),
('市公安', 1),
('市工商小微企部', 2);
----------------------------------------------------------------
-- #t_org
----------------------------------------------------------------


----------------------------------------------------------------
-- #postgres_fdw
-- 需要使用管理员角色执行以下操作
----------------------------------------------------------------
create foreign table ft_org(
  id     int          not null,
  name   varchar(128) not null,
  parent int          null
) server foreign_server options(schema_name 'public', table_name 't_org');
----------------------------------------------------------------
-- #postgres_fdw
----------------------------------------------------------------