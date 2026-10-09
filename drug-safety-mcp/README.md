# 本地药品安全 MCP 服务

本目录实现一个 Node.js stdio MCP 服务，通过 Spring Boot 的本机只读接口查询数据；Node 服务不直连 MySQL。当前仅开放三个工具：

- `search_drug`：在 30 种演示药品范围中检索并返回来源、映射状态和原始证据数量。
- `get_drug_profile`：返回来源与映射元数据；仅唯一 `MATCHED` 时返回 DailyMed 标签信息。
- `get_safety_evidence`：分页读取 FRDB 相互作用或不良事件原始记录，每页最多 10 条。

这些工具用于资料与来源核对，不提供诊断、处方、剂量建议或临床风险结论。详情见仓库根目录的 `ROADMAP.md`。

## 配置与运行

需要 Node.js 20.19+ 或 22.12+、已启动的 Spring Boot 服务，以及已配置并导入演示数据的本地 MySQL。后端须使用相同的本地令牌文件配置；默认位置为本目录的 `.agent-token`。

在仓库根目录的 PowerShell 中执行：

```powershell
Set-Location .\drug-safety-mcp
npm ci
npm run setup-local-token
```

此命令首次运行时生成随机本机令牌；重复运行不会覆盖现有令牌。令牌文件只供本机进程读取，已由 `.gitignore` 排除，不要复制到源码、聊天或日志中。先配置后端数据库和 `JWT_SECRET`，再启动 Spring Boot；服务健康检查通过后，可运行：

```powershell
npm run smoke-test
```

冒烟测试通过 MCP SDK 和 stdio 实际调用三个工具，并检查演示范围、来源映射、分页限制和无令牌拒绝访问。没有运行中的后端或数据库数据时，此测试不会通过。

MCP 宿主应以本目录为工作目录运行 `npm start`，或运行 `node src/server.mjs`。后端默认地址为 `http://127.0.0.1:8081`；如需本机备用端口，可设置 `DRUG_SAFETY_AGENT_BASE_URL`，且只接受 loopback HTTP 地址。

## 安全边界

- 三个工具都声明为只读；Spring 端要求回环来源、独立本机令牌和 GET 请求。
- 查询固定限制在 30 种演示药品；分页大小上限为 10。
- `MATCHED` 还须对应唯一 DailyMed 标签记录才会返回标签；其他映射状态不会当作已确认标签。
- FRDB 相互作用目标和不良事件字段是来源库原始内容，不推断药品间临床结论或患者个体用药建议。
- 数据集、令牌、数据库凭据和日志由使用者保留在本机，不提交到版本库。
