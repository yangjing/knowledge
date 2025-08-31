from typing import Any

from playwright.async_api import Cookie


def cookie_to_dict(cookie: Cookie) -> dict[str, str]:
  c = {
    'name': cookie.name,
    'value': cookie.value,
  }
  if cookie.expires:
    c['expires'] = str(cookie.expires)
  if cookie.httpOnly:
    c['httpOnly'] = str(cookie.httpOnly)
  if cookie.secure:
    c['secure'] = str(cookie.secure)
  if cookie.sameSite:
    c['sameSite'] = cookie.sameSite
  if cookie.domain:
    c['domain'] = cookie.domain
  if cookie.path:
    c['path'] = cookie.path
  return c


def clean_cookie(cookie: dict[str, Any]) -> dict[str, Any]:
  if cookie['sameSite'] is None:
    del cookie['sameSite']
  else:
    same_site = cookie['sameSite'].lower()
    if same_site == 'strict':
      cookie['sameSite'] = 'Strict'
    elif same_site == 'lax':
      cookie['sameSite'] = 'Lax'
    elif same_site == 'none':
      cookie['sameSite'] = 'None'
    else:
      del cookie['sameSite']

  return cookie
