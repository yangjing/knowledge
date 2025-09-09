# PGXN

**<pgxn.org>**

## 更新扩展

### 安装新版本

直接使用 `pgxn install <extension_name>` 将安装最新版本的扩展。

然后在数据库中执行相应的 `alter extension <extension_name> update` 命令升级扩展。

解决方法如下：

1.  **确认扩展文件已更新**：首先确保 PGXN 成功将新版本的扩展文件（如 `.so` 文件、`.sql` 脚本等）安装到了 PostgreSQL 的扩展目录（通常是 `share/extension/`）。
2.  **连接到目标数据库**：使用 `psql` 或其他客户端工具连接到需要升级扩展的那个数据库。
3.  **执行升级命令**：在数据库中运行 SQL 命令来升级扩展。命令格式如下：
    ```sql
    ALTER EXTENSION extension_name UPDATE;
    ```
    将 `extension_name` 替换为实际的扩展名称（例如 `hll`）。
4.  **指定目标版本（可选）**：如果需要升级到特定版本，可以使用：
    ```sql
    ALTER EXTENSION extension_name UPDATE TO 'target_version';
    ```
    例如：`ALTER EXTENSION hll UPDATE TO '2.17';`

执行此命令后，PostgreSQL 会应用必要的更新脚本，将数据库中该扩展的版本号更新到与新安装的文件匹配的版本。

**总结**：PGXN 负责管理服务器上的扩展文件，而 `ALTER EXTENSION ... UPDATE` 命令负责更新数据库内部对该扩展的记录和结构。两者都需要执行才能完成完整的升级。
