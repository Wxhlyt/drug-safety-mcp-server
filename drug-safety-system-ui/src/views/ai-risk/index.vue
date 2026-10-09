<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDrugOptions } from '../../api/drug'
import { analyzeAiRisk, analyzeSourceEvidence } from '../../api/aiRisk'
import { formatSourceTerm } from '../../utils/sourceTranslations'

const loading = ref(false)
const analyzing = ref(false)
const drugList = ref([])

const selectedDrugId = ref(null)
const mode = ref('source')
const sourceResult = ref(null)
const sourceUrl = value => {
  const first = String(value || '').split('|')[0].trim()
  return /^https?:\/\//i.test(first) ? first : null
}

const handleDrugChange = () => {
  sourceResult.value = null
  result.visible = false
}

const result = reactive({
  visible: false,
  drugName: '',
  riskLevel: '',
  riskScore: 0,
  riskReason: '',
  analysisContent: '',
  riskSuggestion: ''
})

const riskLevelMap = {
  LOW: { label: '低风险', type: 'success' },
  MEDIUM: { label: '中风险', type: 'warning' },
  HIGH: { label: '高风险', type: 'danger' }
}

const labelStatusText = {
  CONFIRMED: '唯一确认，可展示标签',
  PENDING_REVIEW: '映射待核验，不展示为已确认标签',
  NO_CONFIRMED_LABEL: '未找到可确认标签'
}
const evidenceStatusText = { HAS_RECORDS: '有原始记录', NO_RECORDS: '无原始记录' }
const displayPolicyText = {
  CONFIRMED_LABEL_AND_EVIDENCE: '唯一确认标签 + 现有原始证据',
  RAW_EVIDENCE_ONLY: '仅展示原始证据，不确认标签',
  SOURCE_METADATA_ONLY: '仅展示来源元数据'
}

const fetchData = async () => {
  loading.value = true
  try {
    const drugRes = await getDrugOptions()
    if (drugRes.code === 200) {
      drugList.value = drugRes.data || []
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const searchDrugs = async (keyword) => {
  try {
    const drugRes = await getDrugOptions(keyword)
    if (drugRes.code === 200) drugList.value = drugRes.data || []
  } catch (error) {
    ElMessage.error('搜索药品失败')
  }
}

const handleSourceAnalyze = async () => {
  if (!selectedDrugId.value) {
    ElMessage.warning('请选择药品')
    return
  }
  analyzing.value = true
  sourceResult.value = null
  try {
    const response = await analyzeSourceEvidence(selectedDrugId.value)
    if (response.code !== 200) throw new Error(response.message || '来源证据分析失败')
    sourceResult.value = response.data
  } catch (error) {
    // 请求封装已展示服务端或网络错误。
  } finally {
    analyzing.value = false
  }
}

const handleAnalyze = async () => {
  if (!selectedDrugId.value) {
    ElMessage.warning('请选择药品')
    return
  }

  analyzing.value = true
  try {
    // 风险计算已迁移到后端，以后端返回结果为准
    const analyzeRes = await analyzeAiRisk({ drugId: selectedDrugId.value })
    if (analyzeRes.code === 200) {
      Object.assign(result, {
        visible: true,
        drugName: analyzeRes.data?.drugName || '',
        riskLevel: analyzeRes.data?.riskLevel || '',
        riskScore: analyzeRes.data?.riskScore ?? 0,
        riskReason: analyzeRes.data?.riskReason || '',
        analysisContent: analyzeRes.data?.analysisContent || '',
        riskSuggestion: analyzeRes.data?.riskSuggestion || ''
      })
      ElMessage.success('分析完成')
    } else {
      ElMessage.error(analyzeRes.message || '分析失败')
    }
  } catch (error) {
    // 请求封装已展示服务端或网络错误。
  } finally {
    analyzing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="ai-risk-container" v-loading="loading">
    <div class="page-header">
      <h2>来源证据分析</h2>
    </div>

    <el-card class="analyze-card" shadow="hover">
      <el-radio-group v-model="mode" class="analysis-mode">
        <el-radio-button label="外部来源证据状态分析" value="source" />
        <el-radio-button label="已核实业务记录评分（仅适用于人工核实记录）" value="score" />
      </el-radio-group>
      <el-alert v-if="mode === 'source'" type="info" :closable="false" show-icon
        title="按固定规则核对 FRDB 来源、标签映射及原始资料状态；不调用 AI 模型，不按资料条数生成临床风险等级或评分。" />
      <el-alert v-else type="warning" :closable="false" show-icon
        title="旧评分仅适用于人工核实的风险或不良反应业务记录；FRDB 原始资料不得当作患者病例参与评分。" />
      <div class="analyze-form">
        <el-form label-width="100px">
          <el-form-item label="药品名称">
            <el-select
              v-model="selectedDrugId"
              filterable
              remote
              reserve-keyword
              :remote-method="searchDrugs"
              placeholder="输入药品名称首字母或前缀搜索"
              clearable
              @change="handleDrugChange"
              style="width: 360px"
            >
              <el-option
                v-for="drug in drugList"
                :key="drug.id"
                :label="drug.drugName"
                :value="drug.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="analyzing" @click="mode === 'source' ? handleSourceAnalyze() : handleAnalyze()">
              开始分析
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card v-if="mode === 'source' && sourceResult" class="result-card" shadow="hover">
      <template #header>外部来源证据状态分析结果</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="药品">{{ sourceResult.drugName }}（ID {{ sourceResult.drugId }}）</el-descriptions-item>
        <el-descriptions-item label="FRDB 化合物 ID">{{ sourceResult.frdbCompoundId }}</el-descriptions-item>
        <el-descriptions-item label="数据版本">{{ sourceResult.datasetVersion }}</el-descriptions-item>
        <el-descriptions-item label="FRDB 映射原值">{{ formatSourceTerm(sourceResult.mappingStatus, 'mappingStatus') }}；候选 {{ sourceResult.candidateCount }} 个</el-descriptions-item>
        <el-descriptions-item label="数据来源" :span="2">
          <el-link v-if="sourceUrl(sourceResult.sourceUrl)" :href="sourceUrl(sourceResult.sourceUrl)" target="_blank" rel="noopener noreferrer">
            {{ sourceResult.sourceName || sourceResult.sourceUrl }}
          </el-link>
          <span v-else>{{ sourceResult.sourceName || '未提供' }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="sourceResult.confirmedLabel" label="已确认 DailyMed 标签" :span="2">
          版本 {{ sourceResult.confirmedLabelVersion || '未提供' }}；
          <el-link v-if="sourceUrl(sourceResult.confirmedLabelUrl)" :href="sourceUrl(sourceResult.confirmedLabelUrl)" target="_blank" rel="noopener noreferrer">查看官方标签原文</el-link>
          <span v-else>来源链接未提供</span>
        </el-descriptions-item>
      </el-descriptions>
      <div class="rule-grid">
        <div class="rule-item">
          <strong>① 标签状态</strong>
          <el-tag :type="sourceResult.labelStatus === 'CONFIRMED' ? 'success' : 'warning'">
            {{ labelStatusText[sourceResult.labelStatus] || '映射待核验' }}
          </el-tag>
          <p>规则：映射为 MATCHED、候选数为 1，且 SPL 集合 ID 和版本均存在，才确认 DailyMed 标签；其余状态不作为已确认标签展示。</p>
        </div>
        <div class="rule-item">
          <strong>② DDI 原始资料状态</strong>
          <span>{{ evidenceStatusText[sourceResult.ddiStatus] || '待核对' }}：{{ sourceResult.ddiCount }} 条；{{ sourceResult.ddiHasSourceLink ? '有' : '无' }}非空来源链接（{{ sourceResult.ddiWithSourceCount }} 条）</span>
          <p>规则：仅检查是否存在原始记录和可追溯链接；记录数量不代表相互作用强弱。</p>
        </div>
        <div class="rule-item">
          <strong>③ 不良事件原始资料状态</strong>
          <span>{{ evidenceStatusText[sourceResult.adverseStatus] || '待核对' }}：{{ sourceResult.adverseCount }} 条；{{ sourceResult.adverseHasSourceLink ? '有' : '无' }}非空来源链接（{{ sourceResult.adverseWithSourceCount }} 条）</span>
          <p>规则：仅检查是否存在原始记录和可追溯链接；记录数量不代表药品更安全或更危险。</p>
        </div>
        <div class="rule-item">
          <strong>④ 展示策略</strong>
          <span>{{ displayPolicyText[sourceResult.displayPolicy] || '仅展示来源元数据' }}</span>
          <p>规则：标签唯一确认时可展示标签与现有证据；否则有原始记录就仅展示原始证据；两类记录均无时仅展示来源元数据。不进行加分汇总。</p>
        </div>
      </div>
      <el-alert type="warning" :closable="false" show-icon :title="sourceResult.limitation" />
      <el-collapse class="samples">
        <el-collapse-item :title="`展开 DDI 原始记录示例（最多 5 条；总计 ${sourceResult.ddiCount} 条）`" name="ddi">
          <el-table v-if="sourceResult.ddiSamples?.length" :data="sourceResult.ddiSamples" border>
        <el-table-column prop="frdb_ddi_id" label="FRDB 记录 ID" width="150" />
        <el-table-column prop="ddi_target" label="原始靶点" min-width="160" />
        <el-table-column label="原始关系" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ formatSourceTerm(row.ddi_relation, 'ddiRelation') }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="140">
          <template #default="{ row }">
            <el-link v-if="sourceUrl(row.ddi_url)" :href="sourceUrl(row.ddi_url)" target="_blank" rel="noopener noreferrer">查看来源</el-link>
            <span v-else>未提供</span>
          </template>
        </el-table-column>
          </el-table>
          <el-empty v-else description="无原始记录" />
        </el-collapse-item>
        <el-collapse-item :title="`展开不良事件原始记录示例（最多 5 条；总计 ${sourceResult.adverseCount} 条）`" name="adverse">
          <el-table v-if="sourceResult.adverseSamples?.length" :data="sourceResult.adverseSamples" border>
        <el-table-column prop="frdb_adverse_event_id" label="FRDB 记录 ID" width="150" />
        <el-table-column label="原始事件类型" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_type, 'adverseType') }}</template>
        </el-table-column>
        <el-table-column label="原始严重程度" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_severity, 'adverseSeverity') }}</template>
        </el-table-column>
        <el-table-column label="来源" min-width="140">
          <template #default="{ row }">
            <el-link v-if="sourceUrl(row.toxicity_source_uri)" :href="sourceUrl(row.toxicity_source_uri)" target="_blank" rel="noopener noreferrer">查看来源</el-link>
            <span v-else>未提供</span>
          </template>
        </el-table-column>
          </el-table>
          <el-empty v-else description="无原始记录" />
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card v-if="mode === 'score' && result.visible" class="result-card" shadow="hover">
      <template #header>
        <div class="result-header">
          <span>分析结果</span>
          <el-tag :type="riskLevelMap[result.riskLevel]?.type || 'info'" size="large">
            {{ riskLevelMap[result.riskLevel]?.label || '未知' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="药品名称">{{ result.drugName }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="riskLevelMap[result.riskLevel]?.type || 'info'">
            {{ riskLevelMap[result.riskLevel]?.label || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险评分">
          <span class="risk-score">{{ result.riskScore }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="风险原因">{{ result.riskReason }}</el-descriptions-item>
        <el-descriptions-item v-if="result.analysisContent" label="AI分析内容">
          {{ result.analysisContent }}
        </el-descriptions-item>
        <el-descriptions-item v-if="result.riskSuggestion" label="风险建议">
          {{ result.riskSuggestion }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.ai-risk-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.analyze-card {
  margin-bottom: 20px;
}

.analyze-form {
  max-width: 600px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.result-card {
  margin-top: 20px;
}

.risk-score {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
.analysis-mode { margin-bottom: 16px; }
.rule-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 12px; margin: 18px 0; }
.rule-item { border: 1px solid #dcdfe6; border-radius: 6px; padding: 14px; line-height: 1.6; }
.rule-item strong { display: block; margin-bottom: 8px; }
.rule-item p { color: #606266; font-size: 13px; margin: 8px 0 0; }
.samples { margin-top: 18px; }
</style>
