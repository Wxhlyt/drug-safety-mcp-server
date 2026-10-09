import assert from 'node:assert/strict';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';

const root = resolve(fileURLToPath(new URL('../', import.meta.url)));
const transport = new StdioClientTransport({
  command: process.execPath,
  args: [resolve(root, 'src/server.mjs')],
  cwd: root,
  stderr: 'pipe'
});
const client = new Client({ name: 'drug-safety-smoke-test', version: '0.1.0' });

function payload(result) {
  assert.equal(result.isError, undefined, result.content?.[0]?.text);
  return result.structuredContent ?? JSON.parse(result.content[0].text);
}

async function expectedError(name, args, messagePattern) {
  let result;
  try {
    result = await client.callTool({ name, arguments: args });
  } catch (error) {
    // Zod validation can reject bad input at the protocol boundary before the handler runs.
    if (messagePattern) throw error;
    assert.match(String(error), /invalid|validation|expected|enum|too_big|too_small/i);
    return;
  }
  assert.equal(result.isError, true);
  assert.ok(result.content?.[0]?.text);
  if (messagePattern) assert.match(result.content[0].text, messagePattern);
}

try {
  await client.connect(transport);
  const listed = await client.listTools();
  const names = listed.tools.map(tool => tool.name).sort();
  assert.deepEqual(names, ['get_drug_profile', 'get_safety_evidence', 'search_drug']);
  assert.ok(client.getInstructions()?.includes('不是患者诊断'));
  assert.ok(listed.tools.every(tool => tool.annotations?.readOnlyHint === true &&
    tool.annotations?.destructiveHint === false));
  console.log('MCP 初始化与 3 个工具枚举：通过');

  await expectedError('get_drug_profile', { drug_id: 1 }, /30 种演示药品范围/);
  await expectedError('get_safety_evidence', {
    drug_id: 26, evidence_type: 'invalid', page: 1, page_size: 10
  });
  await expectedError('get_safety_evidence', {
    drug_id: 26, evidence_type: 'ddi', page: 1, page_size: 11
  });
  console.log('无效 ID、非法证据类型及超出分页上限：通过');

  const search = payload(await client.callTool({
    name: 'search_drug', arguments: { keyword: 'BELINOSTAT' }
  }));
  assert.equal(search.count, 1);
  assert.equal(search.items[0].drug_id, 26);
  assert.equal(search.items[0].drug_name, 'BELINOSTAT');
  assert.ok(search.items[0].evidence_counts.ddi > 0);
  assert.ok(search.items[0].evidence_counts.adverse > 0);
  console.log('BELINOSTAT 搜索与证据计数：通过');

  const profile = payload(await client.callTool({
    name: 'get_drug_profile', arguments: { drug_id: 26 }
  }));
  assert.equal(profile.drug_id, 26);
  assert.equal(profile.mapping_status, 'MATCHED');
  assert.ok(profile.dailymed?.section_count > 0);
  assert.ok(profile.dailymed?.source_url);
  console.log('BELINOSTAT 来源与 DailyMed 章节标题：通过');

  for (const evidenceType of ['ddi', 'adverse']) {
    const evidence = payload(await client.callTool({
      name: 'get_safety_evidence',
      arguments: { drug_id: 26, evidence_type: evidenceType, page: 1, page_size: 2 }
    }));
    assert.equal(evidence.drug_id, 26);
    assert.equal(evidence.evidence_type, evidenceType);
    assert.equal(evidence.items.length, 2);
    assert.ok(evidence.total >= 2);
    assert.ok(evidence.items.every(item =>
      evidenceType === 'ddi' ? 'ddi_url' in item : 'toxicity_source_uri' in item));
    console.log(`BELINOSTAT ${evidenceType} 原始证据分页：通过`);
  }

  const ambiguous = payload(await client.callTool({
    name: 'get_drug_profile', arguments: { drug_id: 1550 }
  }));
  assert.equal(ambiguous.mapping_status, 'AMBIGUOUS');
  assert.equal(ambiguous.dailymed, null);
  console.log('非唯一映射不提供已确认 DailyMed 标签：通过');

  const base = process.env.DRUG_SAFETY_AGENT_BASE_URL || 'http://127.0.0.1:8081';
  const unauthorized = await fetch(new URL('/agent-query/v1/search?keyword=BELINOSTAT', base));
  assert.equal(unauthorized.status, 401);
  console.log('Spring 智能体接口无令牌拒绝访问：通过');
  console.log('SDK → Node stdio → Spring 服务 smoke test：全部通过');
} catch (error) {
  console.error('SDK smoke test 未通过：', error.message);
  process.exitCode = 1;
} finally {
  await client.close();
}
