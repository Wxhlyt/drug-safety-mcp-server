# 药品安全管理系统

这是一个本科课程设计原型，用于演示药品信息管理、来源证据追溯和本地只读 MCP 工具调用。项目不面向真实医疗环境，不提供诊断、处方、剂量建议或临床风险结论。

> 当前状态快照：2026-10-09。实现、验证与计划的边界见 [ROADMAP.md](ROADMAP.md)。

## 当前实现

- **基础管理系统**：Spring Boot 后端与 Vue 前端，包含用户认证与角色权限、药品信息、风险记录、不良反应记录及规则化风险分析。
- **来源证据状态分析**：展示导入来源、FRDB 原始记录数量及来源链接，并区分 DailyMed 映射状态。只有唯一的 `MATCHED` 映射才作为已确认标签展示；其他状态不提供已确认标签。
- **本地 MCP 服务**：Node.js stdio 服务提供 `search_drug`、`get_drug_profile`、`get_safety_evidence` 三个只读工具，限制在 30 种演示药品。服务访问本机 Spring 接口，不直接连接数据库。

风险分析按固定规则处理系统记录；它不是 AI 模型或临床审查。MCP 工具展示来源与原始证据，不会把 `ddi_target` 等字段解释为患者个体用药结论。CLI 工具、临床核查工具和 CLI/MCP 对比实验尚未实现。

## 目录

| 路径 | 内容 |
| --- | --- |
| `drug-safety-system/` | Spring Boot API、数据库结构和 Java 测试 |
| `drug-safety-system-ui/` | Vue 前端及来源字段翻译测试 |
| `drug-safety-mcp/` | 本地 MCP 服务、令牌初始化与端到端冒烟测试 |
| `ROADMAP.md` | 实现状态、验证记录和后续计划 |

FRDB、DailyMed 原始文件和本地数据库不随代码库提供。使用来源证据与 MCP 查询前，需在本机配置数据库并导入所需演示数据。

## 本地运行

### 环境

- JDK 21、Maven 3.8+
- MySQL 8
- Node.js 20.19+ 或 22.12+

### 后端

先按 `drug-safety-system/src/main/resources/db/` 下的 SQL 文件准备本地数据库。通过环境变量提供数据库连接和 JWT 签名密钥；不要把凭据写入源码、配置文件或提交历史。

PowerShell 示例（请在当前终端中填入自己的值，不要提交到 Git）：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/drug_safety?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = '<本机数据库密码>'
$env:JWT_SECRET = '<至少 32 个 UTF-8 字节的随机密钥>'
Set-Location .\drug-safety-system
mvn spring-boot:run
```

`JWT_SECRET` 缺失或短于 32 字节时，JWT 工具会拒绝初始化。首次初始化数据库后，应立即为种子管理员设置本机专用密码；不要将演示凭据用于共享或生产环境。

### 前端

```powershell
Set-Location .\drug-safety-system-ui
npm ci
npm run dev
```

### 本地 MCP

请先运行后端，并确认本机数据库已导入演示范围内的数据，再按 [drug-safety-mcp/README.md](drug-safety-mcp/README.md) 创建本地令牌、注册 MCP 服务并运行冒烟测试。令牌文件、日志和下载的数据不会进入 Git。

## 验证命令

```powershell
# 后端 Java 测试
Set-Location .\drug-safety-system
mvn test

# 前端来源字段翻译测试与生产构建
Set-Location ..\drug-safety-system-ui
node --test test/sourceTranslations.test.js
npm run build

# MCP 冒烟测试（需本机后端、数据库和演示数据）
Set-Location ..\drug-safety-mcp
npm ci
npm run smoke-test
```

2026-10-09 已重新运行后端默认测试（40 项通过）、前端测试（3 项通过）和前端构建；数据库集成测试与 MCP 端到端冒烟测试尚未在本次运行。逐项状态见 [ROADMAP.md](ROADMAP.md)。
