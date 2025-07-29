# uv

## Python install 镜像

```shell
uv python install 3.13.5 --mirror https://github-proxy.lixxing.top/https://github.com/astral-sh/python-build-standalone/releases/download
```

## uv & pip mirrors

```toml
[[tool.uv.index]]
# 也可以设置环境变量：`export UV_CLIENT_PYTHON_RESOLVER=http://pypi.tuna.tsinghua.edu.cn/simple`
name = "tsinghua"
url = "https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple"
default = true
```
