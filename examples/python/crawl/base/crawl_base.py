from typing import TypeVar, Self
from abc import ABC, abstractmethod

T = TypeVar('T')


class CrawlBase[T](ABC):
  @abstractmethod
  async def astart(self) -> Self:
    pass

  @abstractmethod
  async def aclose(self):
    pass

  @abstractmethod
  async def init(self) -> Self:
    pass

  @abstractmethod
  def has_next(self) -> bool:
    pass

  @abstractmethod
  async def run_next(self) -> T | None:
    pass
