# 项目介绍

## 项目初始化

1. 安装 [uv](https://docs.astral.sh/uv/)
2. 安装项目依赖

```shell
uv sync
```

## 爬虫项目

初始化 crawl4ai（及 playwright）环境

```shell
uv run crawl4ai-setup
```

### 运行小红书爬虫

#### 获取账号 cookie

访问 <https://cookie-editor.com/> 安装 cookie-editor 插件。获取登录用户的 cookie，将 cookie 导出为 JSON 格式并保存到 [runs/cookies.json](runs/cookies.json) 文件中。

#### 运行爬虫

```shell
uv run -m crawl.start.crawler-xiaohongshu
```
