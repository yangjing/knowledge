# Conda

## 中国镜像

推荐使用清华大学源：<https://mirrors.tuna.tsinghua.edu.cn/help/anaconda/>

## Miniconda

```shell
wget -c https://mirrors.tuna.tsinghua.edu.cn/anaconda/miniconda/Miniconda3-latest-Linux-x86_64.sh

# 按提示进行安装
chmod +x Miniconda3-latest-Linux-x86_64.sh
./Miniconda3-latest-Linux-x86_64.sh
```

> 安装提示是否设置 shell 时选择：`yes`。然后再手动设置 `conda config --set auto_activate false`（若希望默认加载 conda `base` 环境，可以略过此步骤）。

