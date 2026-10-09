import { randomBytes } from 'node:crypto';
import { openSync, writeFileSync, closeSync, unlinkSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { execFileSync } from 'node:child_process';

const tokenFile = resolve(fileURLToPath(new URL('../.agent-token', import.meta.url)));
let created = false;
function restrictLocalAccess() {
  if (process.platform !== 'win32') return;
  const identity = execFileSync('whoami', ['/user', '/fo', 'csv', '/nh'], { encoding: 'utf8' });
  const sid = identity.match(/S-\d-\d+(?:-\d+)+/)?.[0];
  if (!sid) throw new Error('Cannot resolve current Windows user SID');
  execFileSync('icacls', [tokenFile, '/inheritance:r', '/grant:r',
    `*${sid}:(F)`, '*S-1-5-18:(F)', '*S-1-5-32-544:(F)'], { stdio: 'ignore' });
}
try {
  const descriptor = openSync(tokenFile, 'wx', 0o600);
  created = true;
  try { writeFileSync(descriptor, `${randomBytes(32).toString('base64url')}\n`, 'utf8'); }
  finally { closeSync(descriptor); }
  restrictLocalAccess();
  console.log('本机智能体令牌已创建；未访问或修改数据库。');
} catch (error) {
  if (error.code === 'EEXIST') {
    try {
      restrictLocalAccess();
      console.log('本机智能体令牌已存在；未覆盖，已检查访问权限。');
    } catch {
      console.error('令牌已存在，但无法限制其访问权限；请检查 Windows 文件 ACL。');
      process.exitCode = 1;
    }
  } else {
    if (created) {
      try { unlinkSync(tokenFile); } catch { /* Do not mask the setup failure. */ }
    }
    console.error('无法创建本机智能体令牌；请检查目录写入权限。');
    process.exitCode = 1;
  }
}
