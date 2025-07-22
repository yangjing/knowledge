# Huggingface

## 模型下载

安装 `huggingface_hub`

```shell
pip install huggingface_hub hf-transfer
# uv add --dev huggingface_hub hf-transfer
```

设置环境变量

```shell
export HF_ENDPOINT=https://hf-mirror.com
export HF_HUB_ENABLE_HF_TRANSFER=1
```

下载模型

```shell
huggingface-cli download Qwen/Qwen3-Embedding-0.6B-GGUF --include Qwen3-Embedding-0.6B-Q8_0.gguf
# huggingface-cli download Qwen/Qwen3-Embedding-4B-GGUF --include Qwen3-Embedding-4B-Q4_K_M.gguf
```

## 创建 Ollama 模型

在 `Qwen3-Embedding-0.6B-Q8_0.gguf` 相同目录创建 `Modelfile` 文件，内容如下：

```dockerfile
# 模型文件路径（与 Modelfile 同目录）
FROM Qwen3-Embedding-0.6B-Q8_0.gguf
```

通过 `ollama create` 命令创建并导入模型：

```shell
ollama create Qwen/Qwen3-Embedding-0.6B -f Modelfile
```

测试嵌入模型：

```shell
curl http://localhost:11434/api/embed \
  -X POST \
  -d '{
        "model": "qwen3-embedding:0.6b",
        "input": ["The quick brown fox jumps over the lazy dog."]
      }'
```

> `huggingface-cli` 下载的模型路径类似如下：`/Users/yangjing/.cache/huggingface/hub/models--Qwen--Qwen3-Embedding-0.6B-GGUF/snapshots/370f27d7550e0def9b39c1f16d3fbaa13aa67728`

## 使用 llama.cpp

```shell
docker pull ghcr.io/ggml-org/llama.cpp:server
```

```shell
docker run -d --name=llama-cpp-server -v $HOME/.cache/huggingface/hub:/cache -p 8888:8888 ghcr.io/ggml-org/llama.cpp:server -m /cache/models--Qwen--Qwen3-Embedding-0.6B-GGUF/snapshots/370f27d7550e0def9b39c1f16d3fbaa13aa67728/Qwen3-Embedding-0.6B-Q8_0.gguf --embeddings --pooling mean --port 8888 --host 0.0.0.0 -n 32768
```
