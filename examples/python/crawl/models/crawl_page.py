from datetime import datetime
from typing import Any, Self

from pydantic import BaseModel
from sqlalchemy import TypeDecorator
from sqlalchemy.dialects.postgresql import JSONB
from sqlmodel import Field, SQLModel, Text, BigInteger, ARRAY


class ImageInfo(BaseModel):
  """图片信息数据模型"""

  src: str = Field(max_length=2048, description='图片链接')
  alt: str = Field(default='', max_length=128, description='图片替代文本')
  width: str | None = Field(default=None, max_length=128, description='图片宽度')
  height: str | None = Field(default=None, max_length=128, description='图片高度')
  local_path: str | None = Field(default=None, max_length=1024, description='本地图片路径')
  filename: str | None = Field(default=None, max_length=1024, description='本地图片文件名')


class JsonArrayImageInfo(TypeDecorator):
  """JSON数组图片信息数据模型"""

  impl = JSONB
  cache_ok = True

  def process_bind_param(self, value: list[ImageInfo] | None, dialect) -> list[dict[str, Any]] | None:
    if value is None:
      return None
    return [image_info.model_dump(exclude_none=True) for image_info in value]

  def process_result_value(self, value: Any, dialect) -> list[ImageInfo] | None:
    if value is None:
      return None
    if not isinstance(value, list):
      raise ValueError(f'The value is not a list: {value}')
    return [ImageInfo.model_validate(image_info) for image_info in value]


class CrawlPageBase(SQLModel):
  """文章数据模型"""

  id: str = Field(max_length=2048, primary_key=True, description='ID')
  status: int = Field(default=1, description='状态。1: 新建, 10: 已加载到 Milvus')
  url: str = Field(max_length=2048, primary_key=True, description='文章原始实际访问的url')

  crawl_url: str = Field(max_length=2048, description='要爬取的url')
  title: str = Field(max_length=128, description='文章标题')
  author: str = Field(max_length=128, description='文章作者')
  publish_time: datetime | None = Field(default=None, description='发布时间')
  content: str | None = Field(default=None, sa_type=Text, description='文章内容（HTML格式）')
  markdown: str = Field(sa_type=Text, description='文章内容（Markdown格式）')
  images: list[ImageInfo] = Field(default_factory=list, sa_type=JsonArrayImageInfo, description='文章图片列表')

  created_at: datetime = Field(description='创建（抓取）时间', default_factory=datetime.now)
  updated_at: datetime | None = Field(description='更新时间', default=None)

  def clean_data(self, tail_images: bool = False) -> Self:
    """清理数据"""
    markdown = self.markdown
    if self.images:
      for image_info in self.images:
        if image_info.filename:
          markdown = markdown.replace(image_info.src, image_info.filename)
          self.content = self.content.replace(image_info.src, image_info.filename)

    self.markdown = self._generate_markdown(markdown, False, tail_images)
    return self

  def _generate_markdown(self, markdown: str, has_metadata: bool, tail_images: bool) -> str:
    """生成Markdown内容

    Args:
      markdown: 文章内容
      has_metadata: 是否包含元数据
      tail_images: 是否在文章末尾添加图片链接

    Returns:
      str: 生成的Markdown内容
    """
    if has_metadata:
      markdown = f"""# {self.title}

- **作者:** {self.author}
- **发布时间:** {self.publish_time}
- **原文链接:** {self.url}
- **图片数量:** {len(self.images)}
- **抓取时间:** {self.created_at}

---

{markdown}
"""
    else:
      markdown = f"""# {self.title}

{markdown}
"""

    if tail_images:
      images = ''
      for image_info in self.images:
        if image_info.filename:
          images += f'![{image_info.alt}]({image_info.filename})\n\n'

      markdown += f"""
## 图片

{images}
"""

    return markdown


class CrawlPage(CrawlPageBase, table=True):
  """原始页面数据模型"""

  __tablename__ = 'crawl_page'


class CrawlPageForQuery(BaseModel):
  """原始页面查询数据模型"""

  ids: list[str] | None = Field(default=None, description='文章ID列表')
  id: str | None = Field(default=None, description='文章ID')
  status: list[int] | None = Field(default=None, description='状态列表')
  crawl_urls: list[str] | None = Field(default=None, description='要爬取的url')
  url: str | None = Field(default=None, description='文章实际访问的url')
  title: str | None = Field(default=None, max_length=128, description='文章标题')
  author: str | None = Field(default=None, max_length=128, description='文章作者')
  publish_time_begin: datetime | None = Field(default=None, description='发布时间开始')
  publish_time_end: datetime | None = Field(default=None, description='发布时间结束')
  created_at_begin: datetime | None = Field(default=None, description='创建（抓取）时间开始')
  created_at_end: datetime | None = Field(default=None, description='创建（抓取）时间结束')
  updated_at_begin: datetime | None = Field(default=None, description='更新时间开始')
  updated_at_end: datetime | None = Field(default=None, description='更新时间结束')
  content_length_min: int | None = Field(default=None, description='文章内容长度最小值')
  markdown_length_min: int | None = Field(default=None, description='文章Markdown内容长度最小值')
  title_length_max: int | None = Field(default=None, description='文章标题长度最大值')
  area_id: str | None = Field(default=None, description='地区 ID')
  scenic_id: int | None = Field(default=None, description='景区 ID')
  spot_id: int | None = Field(default=None, sa_type=BigInteger, description='景点 ID')
  similarity: str | None = Field(default=None, description='相似性搜索')

  limit: int | None = Field(default=None, description='查询数量限制')
  pager: str | None = Field(default=None, description='分页ID，查询小于该ID的文章。设置此参数时将强制按 id desc 排序')
  order_by: str | None = Field(default=None, description='排序字段')
