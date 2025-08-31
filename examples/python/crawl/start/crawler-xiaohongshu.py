import json
import os
import asyncio
import logging
from typing import Any

from crawl.models.crawl_model import CrawlAccount, CrawlSite, CrawlTaskParam
from crawl.xiaohongshu import XiaohongshuCrawler
from crawl.config.config import CrawlerConfig


def get_cookies(cookies_path: str = 'runs/cookies.json') -> list[dict[str, Any]]:
  if not os.path.exists(cookies_path):
    return []

  with open(cookies_path, 'r', encoding='utf-8') as f:
    return json.load(f)


async def main():
  config = CrawlerConfig(
    site=CrawlSite(
      id=1,
      name='xiaohongshu',
      output_dir='/tmp/crawl/xiaohongshu',
      headless=False,  # 本地调试时建议打开，服务器上运行时可设置为 True
      max_articles=3,  # 最多抓取 3 篇笔记
    ),
    params=[  # 设置抓取任务，一个关键词一个任务
      CrawlTaskParam(keyword='重庆江津四面山', crawl_site_ids=[1]),
      CrawlTaskParam(keyword='重庆洪崖洞', crawl_site_ids=[1]),
    ],
    accounts=[CrawlAccount(id=1, account='account1', crawl_site_id=1, cookies=get_cookies())],
  )
  async with XiaohongshuCrawler(config) as crawler:
    await crawler.init()
    while crawler.has_next():
      await crawler.run_next()  # 运行下一个任务，
      await asyncio.sleep(5)  # 每篇笔记之间间隔 5 秒


# 执行以下命令启动程序
# uv run -m crawl.start.crawler-xiaohongshu
if __name__ == '__main__':
  logging.basicConfig(level=logging.INFO)
  asyncio.run(main())
