import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const root = resolve(fileURLToPath(new URL('../', import.meta.url)));
const baseUrl = new URL(process.env.DRUG_SAFETY_AGENT_BASE_URL || 'http://127.0.0.1:8081');
if (baseUrl.protocol !== 'http:' || !['127.0.0.1', 'localhost', '[::1]'].includes(baseUrl.hostname)
    || baseUrl.username || baseUrl.password || baseUrl.pathname !== '/' || baseUrl.search || baseUrl.hash) {
  throw new Error('INVALID_LOCAL_BACKEND_URL');
}

function localToken() {
  try {
    const token = readFileSync(resolve(root, '.agent-token'), 'utf8').trim();
    if (token.length >= 32) return token;
  } catch { /* Missing local setup is reported without exposing filesystem details. */ }
  throw new Error('AGENT_TOKEN_NOT_CONFIGURED');
}

async function query(path, params = {}) {
  const url = new URL(path, baseUrl);
  for (const [key, value] of Object.entries(params)) url.searchParams.set(key, String(value));
  let response;
  try {
    response = await fetch(url, {
      method: 'GET',
      headers: { 'X-Drug-Safety-Agent-Token': localToken() },
      signal: AbortSignal.timeout(8000)
    });
  } catch (error) {
    if (error.message === 'AGENT_TOKEN_NOT_CONFIGURED') throw error;
    throw new Error('BACKEND_UNAVAILABLE');
  }
  let payload;
  try { payload = await response.json(); } catch { throw new Error('BACKEND_BAD_RESPONSE'); }
  if (!response.ok) {
    if (['DRUG_OUTSIDE_DEMO_SCOPE', 'DRUG_NOT_FOUND', 'INVALID_KEYWORD',
      'INVALID_EVIDENCE_TYPE', 'INVALID_PAGINATION'].includes(payload?.error)) {
      throw new Error(payload.error);
    }
    if (response.status === 401 || response.status === 403) throw new Error('BACKEND_AUTH_FAILED');
    throw new Error('BACKEND_QUERY_FAILED');
  }
  return payload;
}

export const searchDrug = keyword => query('/agent-query/v1/search', { keyword });
export const getDrugProfile = drugId => query(`/agent-query/v1/drugs/${drugId}/profile`);
export const getSafetyEvidence = (drugId, type, page, size) =>
  query(`/agent-query/v1/drugs/${drugId}/evidence`, { type, page, size });
