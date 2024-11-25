# Linux Network

## 常用网络命令

### 网络常用命令

#### netstat

注意下面命令的区别，当权限不够时不能显示进程号和进程名。

```shell
[ops@test-001 ~]$ netstat -lnp | grep 8500
(Not all processes could be identified, non-owned process info
 will not be shown, you would have to be root to see it all.)
tcp6       0      0 :::8500                 :::*                    LISTEN      -
[ops@test-001 ~]$ sudo netstat -lnp | grep 8500
tcp6       0      0 :::8500                 :::*                    LISTEN      22414/consul
```

## Route

### 向指定目的地址范围（`10.8.0.0/24`）添加路由（`10.0.0.80`）

```
ip route add 10.8.0.0/24 via 10.0.0.80 dev eth0
```

## VIP

### 华为云

官方 VIP 配置文档：<https://support.huaweicloud.com/usermanual-vpc/zh-cn_topic_0067802474.html>

添加 VIP

```
nmcli connection modify "System eth0" ipv4.addresses 192.168.0.180/24
```

删除 VIP

```
nmcli connection modify "System eth0" -ipv4.addresses 192.168.0.180/24
```

或

```
nmcli connection delete "System eth0" ipv4.addresses 192.168.0.180/24
```

**注意：** 需要通过 `ifdown eth0 && ifup eth0` 来重启网卡使 VIP 生效，不需要重启服务器。操作示例如下：

```
[root@test-003 ~]# nmcli connection
NAME         UUID                                  TYPE      DEVICE
System eth0  5fb06bd0-0bb0-7ffb-45f1-d6edd65f3e03  ethernet  eth0
System eth1  9c92fad9-6ecb-3e6c-eb4d-8a47c6f50c04  ethernet  --
System eth2  3a73717e-65ab-93e8-b518-24f5af32dc0d  ethernet  --
System eth3  c5ca8081-6db2-4602-4b46-d771f4330a6d  ethernet  --
System eth4  84d43311-57c8-8986-f205-9c78cd6ef5d2  ethernet  --
[root@test-003 ~]# nmcli connection modify "System eth0" ipv4.addresses 192.168.0.90/24
[root@test-003 ~]# nmcli connection show "System eth0" | grep -F ipv4.addresses
ipv4.addresses:                         192.168.0.90/24
[root@test-003 ~]# ifdown eth0 && ifup eth0
成功断开设备 "eth0"。
连接已成功激活（D-Bus 活动路径：/org/freedesktop/NetworkManager/ActiveConnection/2）
[root@test-003 ~]# ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether fa:16:3e:a2:1b:17 brd ff:ff:ff:ff:ff:ff
    inet 192.168.0.9/24 brd 192.168.0.255 scope global noprefixroute dynamic eth0
       valid_lft 86393sec preferred_lft 86393sec
    inet 192.168.0.90/24 brd 192.168.0.255 scope global secondary noprefixroute eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::f816:3eff:fea2:1b17/64 scope link
       valid_lft forever preferred_lft forever
3: docker0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN group default
    link/ether 02:42:55:e7:1f:42 brd ff:ff:ff:ff:ff:ff
    inet 172.17.0.1/16 brd 172.17.255.255 scope global docker0
       valid_lft forever preferred_lft forever
```
