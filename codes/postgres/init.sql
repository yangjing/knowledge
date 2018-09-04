create user massdata with nosuperuser
  replication
  encrypted password 'Massdata.2018';
create database massdata owner = massdata template = template0 encoding = 'UTF-8' lc_ctype = 'zh_CN.UTF-8' lc_collate = 'zh_CN.UTF-8';
\c massdata;
create extension adminpack;
create extension hstore;

