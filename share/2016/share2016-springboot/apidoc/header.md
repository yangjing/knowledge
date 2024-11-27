## 全局说明

所有API均返回JSON数据，成功返回相应JSON数据，失败返回统一格式如下：

    {
      "errcode": 404,
      "errmsg": "Not Found"
    }

- errcode: Int = 错误码
- errmsg: String = 错误描述
