# CentOS Server Configuration

yum-config-manager --disable pgdg13

## Basic configure

### Locale and Timezone

```
# localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8
# echo 'LANG="zh_CN.UTF-8"' > /etc/locale.conf
```

```
# ln -sf /usr/share/zoneinfo/Asia/Chongqing /etc/localtime
```

### edit /etc/sysctl.conf

```
cat <<'EOF' >> /etc/sysctl.conf
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.core.rmem_default = 16777216
net.core.wmem_default = 16777216
net.core.optmem_max = 40960
net.ipv4.tcp_rmem = 4096 87380 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216
vm.max_map_count = 1048575
vm.swappiness=0
net.core.somaxconn=1024
net.ipv4.tcp_max_tw_buckets=5000
net.ipv4.tcp_max_syn_backlog=1024
fs.inotify.max_user_watches=100000

EOF
```

### edit /etc/security/limits.conf

```
cat <<'EOF' >> /etc/security/limits.conf
root soft nofile 65535
root hard nofile 65535
* soft nofile 65535
* hard nofile 65535

EOF
```

### Close selinux

```
sed -ri s/SELINUX=enforcing/SELINUX=disabled/g  /etc/selinux/config
```

### Static IP

vim `/etc/sysconfig/network-scripts/ifcfg-eth0` (or `ifcfg-xxx`).

```
DEVICE="eth0"
HWADDR="00:21:70:10:7E:CD"
NM_CONTROLLED="no" # Important!
ONBOOT="yes"
BOOTPROTO=static
IPADDR=10.16.1.106
NETMASK=255.255.255.0
#   the GATEWAY is sometimes in: /etc/sysconfig/network
GATEWAY=10.16.1.1
```

In addition, DNS and HOSTNAME can be configured in `/etc/sysconfig/network` file.

```
# Created by anaconda
HOSTNAME=centos7-001
DNS1=8.8.8.8
```

## Install software using yum command

```
yum update && yum install lrzsz vim htop tree ntp epel-release yum-utils
```

启动 NTP 服务

```
systemctl enable --now ntpd
```

### Add PostgreSQL

```
yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
yum-config-manager --enable pgdg-common
yum-config-manager --enable pgdg12
yum-config-manager --disable pgdg14
yum-config-manager --disable pgdg13
yum-config-manager --disable pgdg11
yum-config-manager --disable pgdg10
yum-config-manager --disable pgdg96
```

install PostgreSQL 12

```
sudo yum install -y postgresql12-server postgresql12-contrib
```

Optionally initialize the database and enable automatic start:

```
sudo /usr/pgsql-12/bin/postgresql-12-setup initdb
sudo systemctl enable postgresql-12
sudo systemctl start postgresql-12
```

### Add adoptium

```
cat <<EOF > /etc/yum.repos.d/adoptium.repo
[Adoptium]
name=Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/centos/7/$(uname -m)
enabled=1
gpgcheck=1
gpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public
EOF
```

install jdk11

```
yum install temurin-11-jdk
```

### Add MySQL

```
yum install https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
yum-config-manager --disable mysql57-community
yum-config-manager --enable mysql80-community
```

### Add Nginx(openresty)

<https://openresty.org/en/linux-packages.html#centos>
