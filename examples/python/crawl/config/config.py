#!/usr/bin/env python3
"""
头条文章爬虫配置文件
"""

from pydantic import Field, BaseModel

from crawl.models.crawl_model import CrawlSite, CrawlTaskParam, CrawlAccount


class CrawlerConfig(BaseModel):
  """爬虫配置类"""

  site: CrawlSite = Field(description='爬虫配置')
  params: list[CrawlTaskParam] = Field(description='爬虫关键词')
  accounts: list[CrawlAccount] = Field(description='爬虫账号')
