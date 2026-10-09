<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '../../store'
import { getDrugList } from '../../api/dashboard'

const appStore = useAppStore()
const stats = reactive({ drugCount: null })

onMounted(async () => {
  try {
    const response = await getDrugList()
    if (response.code === 200) {
      stats.drugCount = response.data?.total ?? null
    } else {
      ElMessage.error('加载药品总数失败')
    }
  } catch (error) {
    ElMessage.error('加载药品总数失败')
  }
})
</script>

<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="welcome-title">面向智能体的药品安全信息管理系统</h1>
      <p class="welcome-subtitle">药品资料与外部来源证据查询演示平台</p>
      <p class="welcome-user">欢迎回来，{{ appStore.userInfo?.username || '用户' }}</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div>
              <div class="stat-value">{{ stats.drugCount ?? '—' }}</div>
              <div class="stat-label">药品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div>
              <div class="stat-value">3</div>
              <div class="stat-label">已接入 MCP 只读工具数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div>
              <div class="stat-value source-value">FRDB v1.5</div>
              <div class="stat-label">外部资料来源版本</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="info-card" shadow="never">
      <template #header><span class="section-title">已接入的查询工具</span></template>
      <div class="tool-list">
        <div class="tool-item"><strong>药品搜索</strong><code>search_drug</code></div>
        <div class="tool-item"><strong>药品资料查询</strong><code>get_drug_profile</code></div>
        <div class="tool-item"><strong>来源证据查询</strong><code>get_safety_evidence</code></div>
      </div>
      <p class="section-note">以上工具仅查询已有资料，不修改业务记录。</p>
    </el-card>

    <el-card class="info-card" shadow="never">
      <template #header><span class="section-title">来源证据展示规则</span></template>
      <div class="rule-list">
        <div class="rule-item">
          <strong>标签须唯一确认</strong>
          <span>映射唯一确认且标签版本完整时，才展示已确认的 DailyMed 标签。</span>
        </div>
        <div class="rule-item">
          <strong>原始记录保留来源</strong>
          <span>相互作用与不良事件展示原始资料及可追溯链接；记录条数不代表安全性结论。</span>
        </div>
        <div class="rule-item">
          <strong>待核验映射不作确认</strong>
          <span>存在多个候选或需要复核时，只展示来源证据，不标为已确认标签。</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.home-container { min-height: calc(100vh - 140px); }
.welcome-section {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.welcome-title { margin: 0 0 10px; font-size: 28px; font-weight: bold; }
.welcome-subtitle { margin: 0 0 10px; font-size: 16px; }
.welcome-user { margin: 0; font-size: 14px; opacity: 0.85; }
.stats-row { margin-bottom: 20px; }
.stat-card { margin-bottom: 20px; }
.stat-content { display: flex; align-items: center; justify-content: center; min-height: 64px; text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; line-height: 1.2; }
.source-value { font-size: 24px; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.info-card { margin-bottom: 20px; }
.section-title { font-size: 18px; font-weight: 600; }
.tool-list, .rule-list { display: grid; gap: 16px; }
.tool-item, .rule-item { display: flex; gap: 16px; align-items: baseline; }
.tool-item strong, .rule-item strong { min-width: 130px; color: #303133; }
.tool-item code { color: #606266; font-family: monospace; }
.rule-item span, .section-note { color: #606266; }
.section-note { margin: 16px 0 0; font-size: 14px; }
@media (max-width: 600px) {
  .welcome-title { font-size: 22px; }
  .tool-item, .rule-item { flex-direction: column; gap: 4px; }
}
</style>
