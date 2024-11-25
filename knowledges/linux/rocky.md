# Rocky Linux

_以 Rocky 9 为例_

## 网络配置

在 Rocky9 中配置静态 IP 有以下几种方法：

### 使用 nmcli 命令

1. **查看网络接口名称**：使用`ip a`命令查看系统中的网络接口名称，通常会看到类似`ens3`、`enp0s3`等名称的网络接口，记住需要配置静态 IP 的网络接口名称.
2. **修改网络接口配置**：使用`nmcli`命令来配置静态 IP，例如，如果网络接口是`ens3`，配置命令如下 ：

  ```bash
  nmcli con modify 'ens3' ifname ens3 ipv4.method manual ipv4.addresses 192.168.2.8/24 gw4 192.168.2.1
  nmcli con modify 'ens3' ipv4.dns 192.168.2.1
  ```

  上述命令中，`ipv4.method manual`表示使用手动配置静态 IP 的方式，`ipv4.addresses`后面跟着静态 IP 地址和子网掩码，`gw4`后面跟着网关地址，`ipv4.dns`后面跟着 DNS 服务器地址。你需要根据实际情况修改这些地址。

3. **重启网络接口**：配置完成后，使用以下命令重启网络接口使配置生效 ：

  ```bash
  nmcli con down 'ens3'
  nmcli con up 'ens3'
  ```

### 使用 nmtui 工具

1. **启动 nmtui**：在终端中输入`sudo nmtui`命令，启动基于文本的网络配置工具.
2. **编辑连接**：在 nmtui 的界面中，选择“Edit a connection”，然后按回车键.
3. **选择网络接口**：使用箭头键选择要配置的网络接口，然后按回车键.
4. **配置静态 IP**：在编辑网络接口的界面中，将“IPv4 CONFIGURATION”设置为“Manual”，然后在“Addresses”字段中输入静态 IP 地址、子网掩码和网关地址，在“DNS servers”字段中输入 DNS 服务器地址 。
5. **保存并退出**：配置完成后，选择“OK”保存配置，然后选择“Back”返回主界面，再选择“Quit”退出 nmtui 工具 。

### 通过修改配置文件

1. **查看网络接口名称**：同样先使用`ip a`命令查看网络接口名称 。
2. **找到网络连接配置文件**：网络连接的配置文件通常位于`/etc/NetworkManager/system-connections/`目录下，找到对应的网络接口配置文件，例如`ens3.nmconnection`.
3. **修改配置文件**：使用文本编辑器打开该配置文件，找到`ipv4`部分，将`method`的值改为`manual`，并添加`addresses`、`gateway`和`dns`等参数，例如 ：

  ```ini
  [ipv4]
  method=manual
  addresses=192.168.2.8/24;192.168.2.9/24;
  gateway=192.168.2.1
  dns=192.168.2.1;223.5.5.5;114.114.114.114
  ```

  这里可以配置多个静态 IP 地址，多个地址之间用分号分隔。

4. **重启网络服务**：修改完成后，保存配置文件，然后使用`sudo systemctl restart NetworkManager`命令重启网络服务使配置生效 。

## 软件安装

### 安装 Rust

```sh
sudo dnf install gcc lld
```

```sh
export RUSTUP_DIST_SERVER="https://rsproxy.cn"
export RUSTUP_UPDATE_ROOT="https://rsproxy.cn/rustup"

curl --proto '=https' --tlsv1.2 -sSf https://rsproxy.cn/rustup-init.sh | sh

cat <<EOF >> ~/.cargo/config.toml
[source.crates-io]
replace-with = 'rsproxy-sparse'
[source.rsproxy]
registry = "https://rsproxy.cn/crates.io-index"
[source.rsproxy-sparse]
registry = "sparse+https://rsproxy.cn/index/"
[registries.rsproxy]
index = "https://rsproxy.cn/crates.io-index"
[net]
git-fetch-with-cli = true
EOF

. "$HOME/.cargo/env"            # For sh/bash/zsh/ash/dash/pdksh
```

#### 安装扩展工具（可选）

安装 cargo-binstall

```sh
wget -c https://github.com/cargo-bins/cargo-binstall/releases/latest/download/cargo-binstall-x86_64-unknown-linux-musl.tgz -O ~/Downloads/cargo-binstall-x86_64-unknown-linux-musl.tgz
tar zxf ~/Downloads/cargo-binstall-x86_64-unknown-linux-musl.tgz -C ~/.cargo/bin/
# 或者
cargo install cargo-binstall
```

```sh
cargo binstall cargo-edit
cargo binstall tokei
```

### 安装 Java

安装 Java 21（需要 root 或 sudo 权限）

```sh
cat <<EOF > /etc/yum.repos.d/adoptium.repo
[Adoptium]
name=Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/rhel/$releasever/$basearch
enabled=1
gpgcheck=1
gpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public
EOF
```

### 安装 Node & pnpm

下载安装

```sh
# 下载 Node.js
mkdir ~/Downloads
wget -c https://nodejs.org/dist/v22.11.0/node-v22.11.0-linux-x64.tar.xz -O ~/Downloads/node-v22.11.0-linux-x64.tar.xz

# 解压 Node.js
mkdir ~/local
tar xf ~/Downloads/node-v22.11.0-linux-x64.tar.xz -C ~/local/

# 配置环境变量
cat <<EOF >> ~/.bash_profile
NODE_HOME=$HOME/local/node-v22.11.0-linux-x64
export PATH=$NODE_HOME/bin:$PATH
EOF

# 刷新环境变量
. ~/.bash_profile

# 使用 npmmirror 加速访问
npm config set registry https://registry.npmmirror.com
```


安装 pnpm（可选）

```sh
npm install -g pnpm
```

### 安装 Miniconda & Python

```sh
mkdir -p ~/miniconda3
wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh -O ~/miniconda3/miniconda.sh
bash ~/miniconda3/miniconda.sh -b -u -p ~/miniconda3
rm ~/miniconda3/miniconda.sh
```

```sh
# After installing, close and reopen your terminal application or refresh it by running the following command:
source ~/miniconda3/bin/activate

# To initialize conda on all available shells, run the following command:
conda init --all
```
