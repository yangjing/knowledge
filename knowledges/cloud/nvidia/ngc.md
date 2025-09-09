# Nvidia 云服务

## FourCastNet

安装部署 FourCastNet

```shell
docker pull nvcr.io/nim/nvidia/fourcastnet:1.0.0

export NGC_CLI_API_KEY=<替换为正式 key>
export LOCAL_NIM_CACHE=~/.cache/nim
mkdir -p $LOCAL_NIM_CACHE

docker run -d --name=fourcastnet \
  --runtime=nvidia --gpus all --shm-size 8g \
  -p 8000:8000 \
  -e NGC_CLI_API_KEY \
  -v $LOCAL_NIM_CACHE:/opt/nim/.cache \
  -u $(id -u) \
  nvcr.io/nim/nvidia/fourcastnet:1.0.0
```

## Brev 使用

```shell
# 使用 SSH 直接登录
brev shell stable-diffusion

# 使用 VSCode 打开
brev open fourcastnet-server

# print workspaces within active organization
brev list

brev list --org nca-18846
```

端口转发

```shell
# 重定向网络端口到本地 <本地>:<远程>
brev port-forward fourcastnet-server --port 8000:8000

# 该命令底层采用 SSH 端口转发技术。如需手动设置端口转发，可使用以下 SSH 命令格式：
ssh -i ~/.brev/brev.pem -p 22 -L LOCAL_PORT:localhost:REMOTE_PORT ubuntu@INSTANCE_IP
```

- ~/.brev/brev.pem 是您的 Brev 私钥
- LOCAL_PORT 是本地机器上的端口号
- REMOTE_PORT 是 Brev 实例上的端口号
- INSTANCE_IP 是您实例的 IP 地址
- ubuntu 是 Brev 实例的默认用户名

