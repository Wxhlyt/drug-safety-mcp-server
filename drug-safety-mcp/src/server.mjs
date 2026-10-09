import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';
import { searchDrug, getDrugProfile, getSafetyEvidence } from './data.mjs';

const server = new McpServer({ name: 'drug-safety-local', version: '0.2.0' }, {
  instructions: '仅查询 30 种演示药品的来源和 FRDB 原始证据。工具结果是资料与来源展示，不是患者诊断、处方或用药建议；不得从原始证据推断临床结论。DailyMed 仅在唯一 MATCHED 时视为已确认标签。'
});

function toolError(error) {
  const known = {
    DRUG_OUTSIDE_DEMO_SCOPE: '药品 ID 不在 30 种演示药品范围内。',
    DRUG_NOT_FOUND: '演示药品在当前数据库中不存在。',
    AGENT_TOKEN_NOT_CONFIGURED: '本机智能体令牌尚未配置；请运行 npm run setup-local-token。',
    BACKEND_UNAVAILABLE: 'Spring Boot 后端不可用；请检查本机服务和 /health。',
    BACKEND_AUTH_FAILED: '本机智能体接口认证失败；请重启更新后的后端并确认令牌文件一致。',
    INVALID_KEYWORD: '搜索关键词无效。',
    INVALID_EVIDENCE_TYPE: '证据类型无效。',
    INVALID_PAGINATION: '分页参数无效。'
  };
  return known[error.message] || '后端资料查询失败；请检查服务日志。';
}

function registerReadTool(name, description, inputSchema, query) {
  server.registerTool(name, {
    description, inputSchema,
    annotations: { readOnlyHint: true, destructiveHint: false, idempotentHint: true, openWorldHint: false }
  }, async (args) => {
    try {
      const payload = await query(args);
      return { content: [{ type: 'text', text: JSON.stringify(payload) }], structuredContent: payload };
    } catch (error) {
      return { isError: true, content: [{ type: 'text', text: toolError(error) }] };
    }
  });
}

registerReadTool(
  'search_drug',
  '在 30 种 FRDB 演示药品中按英文名称搜索，返回来源、映射状态和原始证据计数。',
  { keyword: z.string().trim().min(1).max(80) },
  ({ keyword }) => searchDrug(keyword)
);

registerReadTool(
  'get_drug_profile',
  '读取演示药品来源和映射状态。仅唯一匹配时返回 DailyMed 标签及章节标题。',
  { drug_id: z.number().int().positive() },
  ({ drug_id }) => getDrugProfile(drug_id)
);

registerReadTool(
  'get_safety_evidence',
  '分页读取 FRDB 相互作用或不良事件原始证据，最多返回 10 条；不输出临床结论。',
  {
    drug_id: z.number().int().positive(),
    evidence_type: z.enum(['ddi', 'adverse']),
    page: z.number().int().min(1).max(10000).default(1),
    page_size: z.number().int().min(1).max(10).default(10)
  },
  ({ drug_id, evidence_type, page, page_size }) =>
    getSafetyEvidence(drug_id, evidence_type, page, page_size)
);

const transport = new StdioServerTransport();
await server.connect(transport);
