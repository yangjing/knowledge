import logging
from urllib.parse import urlparse
from typing import TYPE_CHECKING

if TYPE_CHECKING:
  from crawl.crawl_page import CrawlPage

logger = logging.getLogger(__name__)


def dump_notes(notes: dict[str, list['CrawlPage']]):
  """显示结果摘要"""
  for i, (keyword, notes) in enumerate(notes.items()):
    logger.info(f'关键词 {i + 1}: {keyword} 爬取了 {len(notes)} 个笔记')
    for note in notes:
      logger.info(f'笔记: {note.title} - {note.author} ({len(note.images)}张图片)')


def extract_note_id(current_url: str) -> str | None:
  try:
    if '/explore/' in current_url:
      # 提取 explore/ 后面，? 前面的部分作为笔记ID
      start_index = current_url.find('/explore/') + len('/explore/')
      end_index = current_url.find('?', start_index)
      if end_index == -1:
        note_id = current_url[start_index:]
      else:
        note_id = current_url[start_index:end_index]
      return f'1:{note_id}'
    else:
      return None
  except Exception:
    logger.error('提取笔记ID错误', exc_info=True)
    return None


def normalize_url(url: str) -> str:
  """标准化URL"""
  parsed = urlparse(url)
  return f'{parsed.scheme}://{parsed.netloc}{parsed.path}'


# uv run -m aiguide.crawl.xiaohongshu._helper
if __name__ == '__main__':
  url = 'https://www.xiaohongshu.com/explore/6808f5c6000000001d02161f?xsec_token=ABUMmJXKN33PwXo6K5kAbPSGugCzG-bpYHFN7KtMhrD5s=&xsec_source=pc_search&source=web_explore_feed'
  normalized_url = normalize_url(url)
  print(normalized_url)
