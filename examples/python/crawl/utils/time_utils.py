from random import randint
import re
import logging
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)


def parse_standard_time(text: str) -> datetime | None:
  """解析标准时间格式，支持多种常见的时间格式

  支持的格式：
  - 2025-06-25 12:11
  - 2025-06-25 12:11:32
  - 2025-06-25 12
  - 2025-06-25
  - 2025-06-25T12:11
  - 2025-06-25T12:11:32
  - 2025-06-25T12

  Args:
      text: 时间文本字符串

  Returns:
      datetime: 解析后的时间对象，如果解析失败返回None
  """
  if not text or not isinstance(text, str):
    logger.warning(f'要解析的时间为空或不是字符串类型: {type(text)}')
    return None

  # 清理文本，去除多余空格
  text = text.strip()

  # 处理空白字符串
  if not text:
    logger.warning('要解析的时间为空字符串')
    return None

  try:
    # 格式1: YYYY-MM-DD HH:MM:SS
    pattern1 = r'^(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{1,2}):(\d{1,2})$'
    match = re.match(pattern1, text)
    if match:
      year, month, day, hour, minute, second = match.groups()
      return datetime(int(year), int(month), int(day), int(hour), int(minute), int(second))

    # 格式2: YYYY-MM-DD HH:MM
    pattern2 = r'^(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{1,2})$'
    match = re.match(pattern2, text)
    if match:
      year, month, day, hour, minute = match.groups()
      return datetime(int(year), int(month), int(day), int(hour), int(minute))

    # 格式3: YYYY-MM-DD HH
    pattern3 = r'^(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2})$'
    match = re.match(pattern3, text)
    if match:
      year, month, day, hour = match.groups()
      return datetime(int(year), int(month), int(day), int(hour))

    # 格式4: YYYY-MM-DD
    pattern4 = r'^(\d{4})-(\d{1,2})-(\d{1,2})$'
    match = re.match(pattern4, text)
    if match:
      year, month, day = match.groups()
      return datetime(int(year), int(month), int(day))

    # 格式5: YYYY-MM-DDTHH:MM:SS (ISO格式)
    pattern5 = r'^(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2}):(\d{1,2})$'
    match = re.match(pattern5, text)
    if match:
      year, month, day, hour, minute, second = match.groups()
      return datetime(int(year), int(month), int(day), int(hour), int(minute), int(second))

    # 格式6: YYYY-MM-DDTHH:MM (ISO格式)
    pattern6 = r'^(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2})$'
    match = re.match(pattern6, text)
    if match:
      year, month, day, hour, minute = match.groups()
      return datetime(int(year), int(month), int(day), int(hour), int(minute))

    # 格式7: YYYY-MM-DDTHH (ISO格式)
    pattern7 = r'^(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2})$'
    match = re.match(pattern7, text)
    if match:
      year, month, day, hour = match.groups()
      return datetime(int(year), int(month), int(day), int(hour))

    logger.warning(f'无法解析的时间格式: {text}')
    return None

  except ValueError as e:
    logger.error(f'解析时间时发生错误: {text}, 错误: {e}')
    return None


def parse_publish_time(text: str) -> datetime | None:
  """解析发布时间文本，可用于 小红书。支持多种格式：

  - 2024-11-22 (年-月-日)
  - 昨天 22:33 (昨天 时:分)
  - 58 分钟前 (数字 分钟前)
  - 04-22 (月-日，当年)
  - 8小时前 河南 (数字小时前 地点)
  - 1天前 北京 (数字天前 地点)
  - 编辑于 01-14 (编辑于 月-日)
  - 编辑于 昨天 08:49 河南 (编辑于 昨天 时:分 地点)

  Args:
      text: 时间文本字符串

  Returns:
      datetime: 解析后的时间对象

  Raises:
      ValueError: 无法解析的时间格式
  """
  if not text or not isinstance(text, str):
    logger.warning(f'要解析的时间为空或不是字符串类型: {type(text)}')
    return None

  # 清理文本，去除多余空格
  text = text.strip()

  # 处理空白字符串
  if not text:
    logger.warning('要解析的时间为空字符串')
    return None

  # 当前时间
  now = datetime.now()

  # 处理"编辑于"前缀
  if text.startswith('编辑于'):
    # 去掉"编辑于"前缀，递归解析剩余部分
    remaining = text[3:].strip()  # 去掉"编辑于"3个字符
    return parse_publish_time(remaining)

  # 格式1: 完整日期 YYYY-MM-DD
  pattern1 = r'^(\d{4})-(\d{1,2})-(\d{1,2})$'
  match = re.match(pattern1, text)
  if match:
    year, month, day = match.groups()
    return datetime(int(year), int(month), int(day))

  # 格式2: 月-日 MM-DD (当年)，后面可带地点等内容
  pattern2 = r'^(\d{1,2})-(\d{1,2})(?:\s+\S+)?$'
  match = re.match(pattern2, text)
  if match:
    month, day = match.groups()
    year = now.year
    result = datetime(year, int(month), int(day))

    # 如果日期在未来，说明是去年的
    if result > now:
      result = datetime(year - 1, int(month), int(day))

    return result

  # 格式3: 昨天 HH:MM (可能带地点)
  pattern3 = r'^昨天\s+(\d{1,2}):(\d{1,2})(?:\s+\S+)?$'
  match = re.match(pattern3, text)
  if match:
    hour, minute = match.groups()
    yesterday = now - timedelta(days=1)
    return datetime(yesterday.year, yesterday.month, yesterday.day, int(hour), int(minute))

  # 格式4: N分钟前 (可能带地点)
  pattern4 = r'^(\d+)\s*分钟前(?:\s+\S+)?$'
  match = re.match(pattern4, text)
  if match:
    minutes = int(match.group(1))
    return now - timedelta(minutes=minutes)

  # 格式5: N小时前 (可能带地点)
  pattern5 = r'^(\d+)\s*小时前(?:\s+\S+)?$'
  match = re.match(pattern5, text)
  if match:
    hours = int(match.group(1))
    return now - timedelta(hours=hours)

  # 格式6: N天前 (可能带地点)
  pattern6 = r'^(\d+)\s*天前(?:\s+\S+)?$'
  match = re.match(pattern6, text)
  if match:
    days = int(match.group(1))
    return now - timedelta(days=days)

  # 格式7: 今天 HH:MM
  pattern7 = r'^今天\s+(\d{1,2}):(\d{1,2})$'
  match = re.match(pattern7, text)
  if match:
    hour, minute = match.groups()
    return datetime(now.year, now.month, now.day, int(hour), int(minute))

  # 格式8: 前天 HH:MM
  pattern8 = r'^前天\s+(\d{1,2}):(\d{1,2})$'
  match = re.match(pattern8, text)
  if match:
    hour, minute = match.groups()
    day_before_yesterday = now - timedelta(days=2)
    return datetime(
      day_before_yesterday.year, day_before_yesterday.month, day_before_yesterday.day, int(hour), int(minute)
    )

  # 格式9: N秒前
  pattern9 = r'^(\d+)\s*秒前(?:\s+\S+)?$'
  match = re.match(pattern9, text)
  if match:
    seconds = int(match.group(1))
    return now - timedelta(seconds=seconds)

  # 格式10: N周前 / N星期前
  pattern10 = r'^(\d+)\s*(?:周|星期)前(?:\s+\S+)?$'
  match = re.match(pattern10, text)
  if match:
    weeks = int(match.group(1))
    return now - timedelta(weeks=weeks)

  # 格式11: N个月前
  pattern11 = r'^(\d+)\s*个?月前(?:\s+\S+)?$'
  match = re.match(pattern11, text)
  if match:
    months = int(match.group(1))
    # 简单估算：1个月 = 30天
    return now - timedelta(days=months * 30)

  # 格式12: N年前
  pattern12 = r'^(\d+)\s*年前(?:\s+\S+)?$'
  match = re.match(pattern12, text)
  if match:
    years = int(match.group(1))
    # 简单估算：1年 = 365天
    return now - timedelta(days=years * 365)

  # 格式13: 刚刚/刚才 (返回当前时间减去几秒)
  if text in ['刚刚', '刚才']:
    return now - timedelta(seconds=1)

  logger.warning(f'无法解析的时间格式: {text}')
  return None


def rand_swing_ms(ms: int, swing: int = 1000) -> int:
  """随机一个在 milliseconds 左右 swing 范围内的整数"""
  return randint(ms - swing, ms + swing)


def rand_swing(s: float, swing: float = 1.0) -> float:
  """随机一个在 seconds 左右 swing 范围内的浮点数"""
  return rand_swing_ms(int(s * 1000), int(swing * 1000)) / 1000
