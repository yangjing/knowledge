"""
小红书笔记抓取程序
功能：
1. 根据关键词搜索小红书笔记
2. 获取第一屏所有笔记
3. 抓取笔记详情（标题、作者、内容、图片）
4. 下载并保存图片到本地
"""

import logging
import os
from typing import Self, override

from playwright.async_api import async_playwright, Browser, BrowserContext, Playwright

from crawl.base.crawl_base import CrawlBase
from crawl.config.config import CrawlerConfig
from crawl.utils.playwright_utils import save_page
from crawl.models.crawl_page import CrawlPage, CrawlPageBase
from crawl.models.crawl_model import CrawlTaskParam, CrawlAccount


from ._xiaohongshu_keyword_crawler import XiaohongshuKeywordCrawler

logger = logging.getLogger(__name__)


class XiaohongshuCrawler(CrawlBase[int]):
  """小红书笔记爬虫"""

  def __init__(self, config: CrawlerConfig):
    assert config.site.id == 1
    self.get_data: dict[str, list[CrawlPage]] = {}
    self.config = config
    self._ensure_dirs()
    self.browser: Browser | None = None
    self.playwright: Playwright | None = None
    self.crawl_account_context_list: list[tuple[CrawlAccount, BrowserContext]] = []
    self.crawl_accounts_index = 0
    self.pending_keywords: list[CrawlTaskParam] = [k for k in self.config.params]
    self.pending_keywords.reverse()
    self.keyword_crawler: XiaohongshuKeywordCrawler | None = None
    logger.info(f'小红书笔记爬虫配置: {config.site}')

  def _ensure_dirs(self):
    """确保输出目录存在"""
    os.makedirs(self.config.site.output_dir, exist_ok=True)

  async def __aenter__(self):
    """异步上下文管理器入口"""
    await self.astart()
    return self

  async def __aexit__(self, exc_type, exc_val, exc_tb):
    """异步上下文管理器出口"""
    await self.aclose()

  async def astart(self) -> Self:
    """启动浏览器"""
    self.playwright = await async_playwright().start()
    self.browser = await self.playwright.chromium.launch(headless=self.config.site.headless)  # 设为False便于调试
    logger.info('浏览器启动成功')
    return self

  async def aclose(self):
    """关闭浏览器"""
    if self.crawl_account_context_list:
      for _, context in self.crawl_account_context_list:
        await context.close()
    if self.browser:
      await self.browser.close()
      logger.info('浏览器关闭成功')
    if self.playwright:
      await self.playwright.stop()
      logger.info('playwright 关闭成功')

  async def _set_crawl_accounts(self):
    """获取并缓存所有账号"""
    if self.crawl_account_context_list:  # 已设置，
      return

    for crawl_account in self.config.accounts:
      context = await self.browser.new_context(
        viewport=self.config.site.viewport,
        extra_http_headers=self.config.site.extra_http_headers,
      )
      await context.add_cookies(crawl_account.to_cookies())
      self.crawl_account_context_list.append((crawl_account, context))

  def _get_crawl_account(self) -> tuple[CrawlAccount, BrowserContext]:
    """获取下一个账号"""
    entry = self.crawl_account_context_list[self.crawl_accounts_index % len(self.crawl_account_context_list)]
    self.crawl_accounts_index += 1
    return entry

  def get_data(self) -> dict[str, list[CrawlPage]]:
    return self.get_data

  @override
  async def init(self) -> Self:
    await self._set_crawl_accounts()
    logger.info(f'小红书笔记爬虫初始化完成，待抓取的关键词: {[c.keyword for c in self.pending_keywords]}')
    return self

  @override
  def has_next(self) -> bool:
    return (self.keyword_crawler is not None and self.keyword_crawler.has_next()) or self.pending_keywords

  @override
  async def run_next(self) -> CrawlPageBase | None:
    if not self.has_next():
      return None

    if not self.keyword_crawler or not self.keyword_crawler.has_next():
      if not self.pending_keywords:
        return None
      _, context = self._get_crawl_account()
      logger.info(f'Context has page length: {len(context.pages)},  {context}')
      page = await context.new_page()
      keyword = self.pending_keywords.pop()
      if self.keyword_crawler:
        await self.keyword_crawler.aclose()
      self.keyword_crawler = XiaohongshuKeywordCrawler(self.config.site, keyword, page)
      await self.keyword_crawler.astart()

    crawl_page = None
    try:
      crawl_page = await self.keyword_crawler.run_next()
    except Exception as e:
      logger.error(f'小红书笔记抓取异常: {e}')
      error_dir = os.path.join(self.config.site.output_dir, 'errors')
      os.makedirs(error_dir, exist_ok=True)
      await save_page(self.keyword_crawler.page, error_dir, self.keyword_crawler.crawl_param.keyword)

    if not self.keyword_crawler.has_next():
      await self.keyword_crawler.aclose()
      self.keyword_crawler = None

    return crawl_page
