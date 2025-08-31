from datetime import datetime
from enum import IntEnum
from typing import Any

from playwright._impl._api_structures import ViewportSize
from sqlmodel import SQLModel, Field, Text, ARRAY, BigInteger, Integer, TypeDecorator
from pydantic import BaseModel, field_serializer
from sqlalchemy.dialects.postgresql import JSONB

from crawl.utils.cookie_utils import clean_cookie


class CrawlAccountBase(SQLModel):
  """基础站点模型"""

  account: str = Field(description='账号名称')
  crawl_site_id: int = Field(description='站点ID', sa_type=BigInteger, foreign_key='crawl_site.id')
  status: int = Field(description='账号状态。100:启用,99:禁用', default=100)
  cookies: list[dict[str, Any]] = Field(sa_type=JSONB, default=[], description='站点账号的 Cookies')
  created_at: datetime = Field(description='创建时间', default_factory=datetime.now)
  updated_at: datetime | None = Field(default=None, description='更新时间')

  def to_cookies(self) -> list[dict[str, Any]]:
    return list(map(clean_cookie, self.cookies))


class CrawlAccount(CrawlAccountBase, table=True):
  """站点模型"""

  __tablename__ = 'crawl_account'

  id: int = Field(description='账号ID', primary_key=True)


class CrawlTaskParam(BaseModel):
  """爬虫任务参数

  area_id, scenic_id, spot_id 对应 Location 表的 biz_id 字段，通过 biz_kind 的 1,2,3 对应地区、景区、景点
  crawl_site_ids 对应 CrawlSite 表的 id 字段
  """

  keyword: str = Field(max_length=255, description='关键词')
  crawl_site_ids: list[int] | None = Field(
    default=None, description='需要爬取的网站ID列表，不设置使用所有有效的 CrawlSite'
  )
  max_articles: int | None = Field(default=None, description='最大文章数量，不设置则使用 CrawlSite 的配置')


class CrawlStatus(IntEnum):
  """爬虫状态"""

  DISABLED = 99
  """禁用"""

  ENABLED = 100
  """启用"""


class CrawlSiteBase(SQLModel):
  """爬虫配置"""

  name: str = Field(max_length=255, description='爬虫名称', unique=True)
  description: str | None = Field(default=None, description='爬虫描述')
  status: CrawlStatus = Field(default=CrawlStatus.ENABLED, sa_type=Integer, description='爬虫状态')

  headless: bool = Field(default=True, description='是否无头模式')
  viewport: ViewportSize | None = Field(default=None, sa_type=JSONB, description='视窗大小')
  extra_http_headers: dict[str, str] | None = Field(default=None, sa_type=JSONB, description='额外HTTP头')

  # 基本配置
  output_dir: str = Field(description='输出目录')
  max_articles: int = Field(default=10, description='最大文章数量')
  delay_between_requests: float = Field(default=2.0, description='请求间隔时间（秒）')

  # 网络配置
  request_timeout: int = Field(default=30, description='请求超时时间（秒）')
  retry_count: int = Field(default=3, description='重试次数')

  # 图片配置
  download_images: bool = Field(default=True, description='是否下载图片')
  image_format: str = Field(default='jpg', description='图片格式')
  max_image_size: int = Field(default=50 * 1024 * 1024, description='最大图片大小（字节），默认为 50MB')

  # 文章过滤配置
  min_content_length: int = Field(default=100, description='最小文章长度(字符)')
  exclude_keywords: list[str] = Field(default=[], sa_type=ARRAY(Text), description='排除关键词')

  # 输出配置
  save_as_db: bool = Field(default=False, description='是否保存为数据库')
  save_as_json: bool = Field(default=True, description='是否保存为JSON格式')
  save_as_markdown: bool = Field(default=False, description='是否保存为Markdown格式')
  compress_output: bool = Field(default=False, description='是否压缩输出')

  @field_serializer('status')
  def serialize_status(self, value: int | CrawlStatus) -> int:
    """序列化状态字段，确保返回整数值"""
    if isinstance(value, CrawlStatus):
      return value.value
    return value


class CrawlSite(CrawlSiteBase, table=True):
  """爬虫配置"""

  __tablename__ = 'crawl_site'

  id: int = Field(primary_key=True)


class CrawlSiteForQuery(BaseModel):
  """爬虫配置查询"""

  ids: list[int] | None = Field(default=None, description='爬虫配置ID')
  name: str | None = Field(default=None, description='爬虫名称')
  status: CrawlStatus | None = Field(default=None, description='爬虫状态')
  output_dir: str | None = Field(default=None, description='输出目录')
  max_articles: int | None = Field(default=None, description='最大文章数量')
  delay_between_requests: float | None = Field(default=None, description='请求间隔时间（秒）')

  limit: int | None = Field(default=None, description='限制返回条数')
  offset: int | None = Field(default=None, description='偏移量')


class CrawlSiteInfo(BaseModel):
  """爬虫配置信息"""

  site: int | None = Field(default=None, sa_type=BigInteger, description='站点ID')
  # account: int | None = Field(default=None, sa_type=BigInteger, description='站点账号ID')


class JsonArrayCrawlSiteInfo(TypeDecorator):
  """JSON 数组爬虫配置信息"""

  impl = JSONB
  cache_ok = True

  def process_bind_param(self, value: list[CrawlSiteInfo] | None, dialect) -> list[dict[str, Any]] | None:
    if value is None:
      return None
    return [CrawlSiteInfo.model_validate(item).model_dump(exclude_none=True) for item in value]

  def process_result_value(self, value: Any | None, dialect) -> list[CrawlSiteInfo] | None:
    if value is None:
      return None
    if not isinstance(value, list):
      raise ValueError(f'value must be a list, actual type: {type(value)}')
    return [CrawlSiteInfo.model_validate(item) for item in value]
