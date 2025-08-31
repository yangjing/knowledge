from datetime import datetime
import os
import logging

from playwright.async_api import Page

logger = logging.getLogger(__name__)


async def save_page(page: Page, output_dir: str, keyword: str):
  await save_page_screenshot(page, output_dir, keyword)
  await save_page_html(page, output_dir, keyword)


async def save_page_screenshot(page: Page, output_dir: str, keyword: str):
  """保留完整页面为图片并存储为 png 图片到本地文件

  Args:
    page: 页面
    output_dir: 输出目录
    keyword: 关键词
  """

  try:
    # 文件名格式: 关键词_时间戳.png，防止重复
    safe_keyword = keyword.replace('/', '_').replace('\\', '_')
    now = datetime.now().strftime('%Y%m%d%H%M%S')
    filename = f'{safe_keyword}_{now}.png'
    filepath = os.path.join(output_dir, filename)
    await page.screenshot(path=filepath, full_page=True)  # 截取完整页面
    logger.info(f'已保存完整页面截图: {filepath}')
  except Exception as e:
    logger.error(f'保存页面截图失败: {e}')


async def save_page_html(page: Page, output_dir: str, keyword: str):
  """保存页面 HTML 到本地文件

  Args:
    page: 页面
    output_dir: 输出目录
    keyword: 关键词
  """
  try:
    # 文件名格式: 关键词_时间戳.png，防止重复
    safe_keyword = keyword.replace('/', '_').replace('\\', '_')
    now = datetime.now().strftime('%Y%m%d%H%M%S')
    filename = f'{safe_keyword}_{now}.html'
    filepath = os.path.join(output_dir, filename)
    with open(filepath, 'w', encoding='utf-8') as f:
      f.write(await page.content())  # 保存页面 HTML
    logger.info(f'已保存页面 HTML: {filepath}')
  except Exception as e:
    logger.error(f'保存页面 HTML 失败: {e}')
