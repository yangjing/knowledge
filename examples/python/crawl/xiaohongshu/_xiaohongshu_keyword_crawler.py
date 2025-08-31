import asyncio
import os
import logging
from typing import Self
from urllib.parse import urlparse
from random import randint

import httpx
import aiofiles
from crawl4ai import DefaultMarkdownGenerator
from playwright.async_api import Page, Locator

from crawl.utils.time_utils import parse_publish_time, rand_swing, rand_swing_ms
from crawl.models.crawl_page import CrawlPageBase, ImageInfo
from crawl.models.crawl_model import CrawlSite, CrawlTaskParam

from ._helper import normalize_url, extract_note_id

logger = logging.getLogger(__name__)

# 基础配置
EXPLORE_URL = 'https://www.xiaohongshu.com/explore'


class XiaohongshuKeywordCrawler:
  """小红书爬虫关键词"""

  def __init__(self, site: CrawlSite, crawl_param: CrawlTaskParam, page: Page):
    self.site = site
    self.crawl_param = crawl_param
    self.page = page
    self.notes: list[CrawlPageBase] = []
    self.pending_note_items: list[Locator] = []
    self.index = 0

  async def astart(self) -> Self:
    max_notes = self.crawl_param.max_articles or self.site.max_articles
    logger.info(f'🚀 开始爬取小红书笔记，关键词: {self.crawl_param.keyword}，最大数量: {max_notes}')

    # 1. 导航到探索页面
    await self.navigate_to_explore()

    await self.check_login()  # 若未登录，将抛出异常

    # 2. 搜索笔记
    search_success = await self.search_notes(self.crawl_param.keyword)
    if not search_success:
      return self

    # 3. 获取笔记列表
    note_items = await self.get_note_items()
    if not note_items:
      logger.warning('未找到任何笔记')
      return self

    # 4. 限制处理数量
    self.pending_note_items = note_items[:max_notes]
    self.pending_note_items.reverse()
    logger.info(f'准备处理 {len(self.pending_note_items)} 个笔记')

    return self

  async def aclose(self):
    if not self.page.is_closed():
      await self.page.close()

  def has_next(self) -> bool:
    return self.pending_note_items

  async def run_next(self) -> CrawlPageBase | None:
    if not self.has_next():
      return None

    self.index += 1
    note_item = self.pending_note_items.pop()

    # 点击 ESC 键，确保当详情弹窗未关闭时将其关闭
    await self.page.keyboard.press('Escape')

    crawl_page = await self.extract_note_detail(note_item, self.index)
    if crawl_page:
      await self.save_note(crawl_page)
      self.notes.append(crawl_page)

    # 关闭详情弹窗（点击遮罩区域）
    await self._close_modal(escape=True)

  async def _close_modal(self, escape: bool = False):
    await self.page.wait_for_timeout(rand_swing(1))
    close_btn = self.page.locator('.close-circle')
    if await close_btn.count() > 0 and await close_btn.is_visible():
      await close_btn.click()
      logger.info('关闭详情弹窗成功')
    elif escape:
      await self.page.keyboard.press('Escape')
      logger.info('关闭详情弹窗成功，使用 Escape')

  async def navigate_to_explore(self):
    """导航到小红书探索页面"""
    logger.info(f'导航到探索页面: {EXPLORE_URL}')
    await self.page.goto(EXPLORE_URL, wait_until='domcontentloaded')
    await self.page.wait_for_timeout(3000)  # 等待页面完全加载

  async def check_login(self):
    """检查是否登录"""
    login_container = self.page.locator('.login-container')
    login_btn = login_container.get_by_role('button', name='登录')
    if await login_btn.is_visible():
      raise Exception('未登录，需要登录访问')

  async def search_notes(self, keyword: str) -> bool:
    """搜索笔记"""

    try:
      # 1. 定位搜索框并输入关键词
      search_input = self.page.locator('.input-box #search-input')
      await search_input.wait_for(state='visible', timeout=10000)
      await search_input.click()
      await search_input.fill(keyword)

      # 2. 点击搜索按钮
      search_btn = self.page.locator('.input-box .search-icon')
      await search_btn.click()
      await self.page.wait_for_timeout(rand_swing_ms(2000))

      # 3. 点击图文按钮，只显示图文相关笔记
      content_image_btn = self.page.locator('#image')
      await content_image_btn.wait_for(state='visible', timeout=10000)
      await content_image_btn.click()
      await self.page.wait_for_timeout(rand_swing_ms(500))

      # # 4. 点击筛选，按时间排序
      # await self.page.locator('.filter', has_text='筛选').hover()
      # await self.page.get_by_text(re.compile('^筛选$')).hover()
      # await self.page.wait_for_timeout(rand_swing_ms(500))
      # await self.page.locator('.filters span', has_text='最新').click()
      # await self.page.wait_for_timeout(rand_swing_ms(1000))

      logger.info('搜索完成')
      return True

    except Exception as e:
      logger.error(f'搜索失败: {e}')
      return False

  async def get_note_items(self) -> list[Locator]:
    """获取第一屏的所有笔记项"""
    logger.info('获取笔记列表...')
    locators = []

    try:
      # 等待笔记容器出现
      await self.page.wait_for_selector('.feeds-container .note-item', timeout=10000)
      note_items_locator = self.page.locator('.feeds-container .note-item')

      # 获取可见的笔记项数量
      count = await note_items_locator.count()
      logger.info(f'找到 {count} 个笔记')

      for i in range(count):
        locator = note_items_locator.nth(i)
        if await locator.locator('.query-note-list').is_visible():  # 过滤掉搜索项
          continue
        locators.append(locator)

    except Exception as e:
      logger.error(f'获取笔记列表失败: {e}')

    return locators

  async def extract_note_detail(self, note_item_locator: Locator, index: int) -> CrawlPageBase | None:
    """提取单个笔记的详细信息"""
    logger.info(f'正在提取笔记 index: {index}, url: {self.page.url}')

    try:
      # 1. 点击笔记图片打开详情
      img_link = None
      for selector in ['a.title', 'a.cover']:
        link = note_item_locator.locator(selector)
        if await link.count() > 0:
          img_link = link
          logger.info(f'找到笔记封面或标题 selector: {selector}')
          break
      if not img_link:
        raise Exception('无法找到笔记封面或标题')

      # await img_link.evaluate('e => e.click()')  # 直接使用 JS，防止小红书使用遮罩层屏蔽 click 事件
      await img_link.click()

      # 2. 等待详情弹窗出现
      note_container = self.page.locator('.note-container')
      await note_container.wait_for(state='visible', timeout=10000)

      logger.info(f'当前页面url: {self.page.url}')

      current_url = self.page.url

      # 3. 提取笔记 ID
      note_id = extract_note_id(current_url)

      if not note_id:
        raise Exception('无法提取笔记ID')

      url = normalize_url(current_url)

      logger.info(f'index: {index}, note_id: {note_id}, current_url: {current_url}')

      # TODO: 这里可以添加 note_id（或 current_url）是否已抓取逻辑，已抓取过的笔记可以跳过

      # 4. 提取笔记信息
      title = ''
      author = ''
      content = ''
      publish_time = None

      # 获取标题
      title_element = note_container.locator('.note-content .title')
      title = await title_element.inner_text()
      logger.info(f'提取到标题: {title}')

      # 获取图片URLs并创建ImageInfo对象
      image_urls = await self._extract_image_urls(note_container)
      images = []

      for i, img_url in enumerate(image_urls):
        image_info = ImageInfo(src=img_url, alt=f'{title} - 图片{i + 1}' if title else f'图片{i + 1}')
        images.append(image_info)

      # 下载图片
      if self.site.download_images and images:
        await self._download_images(note_id, images)

      # 获取作者
      author_element = note_container.locator('.author-container .username')
      author = await author_element.inner_text()
      logger.info(f'获取到作者：{author}')

      # 获取笔记内容
      content_element = note_container.locator('.note-content .desc')
      content = await content_element.inner_html()

      # 获取发布时间
      try:
        publish_time_element = note_container.locator('.note-content .date')
        publish_time_text = await publish_time_element.inner_text()
        publish_time = parse_publish_time(publish_time_text)
      except Exception as e:
        logger.warning(f'无法获取发布时间信息: {e}')
        publish_time = None

      logger.info(f'发布时间：{publish_time}')

      # 生成markdown内容
      text = await content_element.locator('.note-text span').nth(0).inner_html()
      markdown = self._generate_markdown_from_html(text)

      # 创建CrawlPage对象
      page_data = CrawlPageBase(
        id=note_id,
        url=url,
        crawl_url=current_url,
        title=title,
        author=author,
        publish_time=publish_time,
        content=content,
        markdown=markdown,
        images=images,
      )

      logger.info(f'成功提取笔记: {page_data.title}')
      return page_data

    except Exception:
      return None

  async def _extract_image_urls(self, note_container: Locator) -> list[str]:
    """提取笔记中的所有图片URL"""
    image_urls = []

    try:
      # 定位图片轮播容器
      swiper_wrapper = note_container.locator('.swiper-wrapper')
      await self.page.wait_for_timeout(1000)  # 等待 swiper 元素加载

      # 获取所有图片元素
      img_elements = await swiper_wrapper.locator('div[data-swiper-slide-index]:not(.swiper-slide-duplicate) img').all()
      img_count = len(img_elements)

      logger.info(f'找到图片个数: {img_count}')

      for i in range(img_count):
        try:
          img_element = img_elements[i]
          # if not await img_element.is_visible():  # 只获取可见的图片
          # continue
          src = await img_element.get_attribute('src')
          if not src:
            continue
          if src in image_urls:  # 去重
            logger.info(f'第 {i} 张图片URL已存在: {src}, 跳过')
            continue
          image_urls.append(src)
        except Exception as e:
          logger.warning(f'获取第 {i} 张图片URL失败: {e}')
          continue

      logger.info(f'成功获取图片URL个数: {len(image_urls)}')

    except Exception as e:
      logger.error(f'提取图片URLs失败: {e}')

    return image_urls

  def _generate_markdown_from_html(self, html_content: str) -> str:
    """从HTML内容生成Markdown"""
    try:
      # 1) 实例化生成器（可传过滤器 / 其它选项，后面详述）
      md_gen = DefaultMarkdownGenerator()

      # 2) 生成 Markdown
      parsed_url = urlparse(self.page.url) if self.page else None
      base_url = f'{parsed_url.scheme}://{parsed_url.netloc}' if parsed_url else 'https://xiaohongshu.com'
      res = md_gen.generate_markdown(
        input_html=html_content,
        base_url=base_url,
        options={
          'ignore_links': True,
          'ignore_images': True,
          'ignore_emphasis': True,
        },  # 当前用于 LLM 不需要链接和图片，所以忽略
      )
      markdown = res.fit_markdown or res.raw_markdown or ''
      return markdown
    except Exception as e:
      logger.warning(f'HTML转Markdown失败: {e}')
      return html_content  # 降级返回HTML内容

  async def _download_images(self, note_id: str, images: list[ImageInfo]) -> None:
    """下载图片到本地并更新ImageInfo对象"""
    logger.info(f'[小红书] crawl_site.output_dir = {self.site.output_dir}')
    base_dir = os.path.join(self.site.output_dir, note_id)
    logger.info(f'\n下载图片到目录: {base_dir}\n')
    os.makedirs(base_dir, exist_ok=True)
    # context = self.page.context
    # cookies = await context.cookies()

    async with httpx.AsyncClient(
      headers=self.site.extra_http_headers,
      timeout=httpx.Timeout(self.site.request_timeout or 30),
      # cookies=[cookie_to_dict(c) for c in cookies],
    ) as client:
      for i, image_info in enumerate(images):
        try:
          # 生成文件名
          filename = f'{note_id}_{i + 1}.{self.site.image_format}'
          filepath = os.path.join(base_dir, filename)

          # 下载图片
          await self._download_single_image(client, image_info.src, filepath)
          # 更新ImageInfo对象
          image_info.local_path = filepath
          image_info.filename = filename
          logger.debug(f'图片下载成功: {filename}')
          await asyncio.sleep(randint(50, 200) / 1000)

        except Exception:
          logger.error(f'下载图片异常: {filename}', exc_info=True)
          continue

  async def _download_single_image(self, client: httpx.AsyncClient, img_url: str, filepath: str):
    """下载单张图片"""
    response = await client.get(img_url)
    if response.status_code != 200:
      raise Exception(f'图片下载失败，状态码: {response.status_code}')

    content = response.content

    # 检查图片大小限制
    if self.site.max_image_size is not None and len(content) > self.site.max_image_size:
      raise Exception(f'图片超出大小限制({len(content)} > {self.site.max_image_size}字节)')

    async with aiofiles.open(filepath, 'wb') as f:
      await f.write(content)

  async def save_note(self, crawl_page: CrawlPageBase):
    """保存笔记数据"""
    filename_base = crawl_page.id or 'unknown'
    base_dir = os.path.join(self.site.output_dir, filename_base)
    os.makedirs(base_dir, exist_ok=True)

    crawl_page.clean_data()

    if self.site.save_as_db:
      # TODO: 添加保存笔记到数据库或其它持久化存储逻辑
      pass

    # 保存为JSON
    if self.site.save_as_json:
      json_path = os.path.join(base_dir, f'{filename_base}.json')
      async with aiofiles.open(json_path, 'w', encoding='utf-8') as f:
        await f.write(crawl_page.model_dump_json(indent=2))
      logger.info(f'JSON保存成功: {json_path}')

    # 保存为Markdown
    if self.site.save_as_markdown:
      md_path = os.path.join(base_dir, f'{filename_base}.md')
      async with aiofiles.open(md_path, 'w', encoding='utf-8') as f:
        await f.write(crawl_page.markdown)
      logger.info(f'Markdown保存成功: {md_path}')
