<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDrugList, getExternalDrugOverview, getExternalDrugDdi, getExternalDrugAdverse } from '../api/drug'
import { formatSourceTerm } from '../utils/sourceTranslations'

const props = defineProps({ kind: { type: String, required: true } })
const drugs = ref([])
const drugId = ref(null)
const overview = ref(null)
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
let requestId = 0

const isRisk = computed(() => props.kind === 'risk')
const selectedDrug = computed(() => drugs.value.find(item => item.id === drugId.value))
const sourceUrl = value => {
  const first = String(value || '').split('|')[0].trim()
  return /^https?:\/\//i.test(first) ? first : null
}
const showValue = value => value === null || value === undefined || value === '' ? '未提供' : value

const fetchEvidence = async () => {
  if (!drugId.value) return
  const currentRequest = ++requestId
  loading.value = true
  try {
    const fetchPage = isRisk.value ? getExternalDrugDdi : getExternalDrugAdverse
    const [overviewResponse, pageResponse] = await Promise.all([
      getExternalDrugOverview(drugId.value),
      fetchPage(drugId.value, { page: page.value, size: size.value })
    ])
    if (currentRequest !== requestId) return
    if (overviewResponse.code !== 200 || pageResponse.code !== 200) {
      throw new Error(overviewResponse.message || pageResponse.message || '来源数据加载失败')
    }
    overview.value = overviewResponse.data
    items.value = pageResponse.data?.items || []
    total.value = pageResponse.data?.total || 0
  } catch (error) {
    if (currentRequest !== requestId) return
    overview.value = null
    items.value = []
    total.value = 0
    ElMessage.error(error?.message || '来源数据加载失败')
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

const selectDrug = () => { page.value = 1; fetchEvidence() }
const changeSize = value => { size.value = value; page.value = 1; fetchEvidence() }
const changePage = value => { page.value = value; fetchEvidence() }

onMounted(async () => {
  loading.value = true
  try {
    const response = await getDrugList()
    if (response.code !== 200) throw new Error(response.message || '药品列表加载失败')
    drugs.value = (response.data || [])
      .filter(item => item.dataSource === 'FRDB')
      .sort((a, b) => a.drugName.localeCompare(b.drugName, 'en'))
    drugId.value = drugs.value[0]?.id || null
    if (drugId.value) await fetchEvidence()
  } catch (error) {
    ElMessage.error(error?.message || '药品列表加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <el-alert
      type="info" :closable="false" show-icon
      :title="isRisk
        ? '展示 FRDB 药物相互作用原始证据；靶点和关系不是临床风险等级，也不代表两种系统药品可以直接关联。'
        : '展示 FRDB 不良事件原始证据；事件类型和严重程度是来源字段，不等于患者病例或具体症状。'"
    />
    <div class="toolbar">
      <span>药品</span>
      <el-select v-model="drugId" filterable placeholder="搜索药品名称" style="width: 320px" @change="selectDrug">
        <el-option v-for="drug in drugs" :key="drug.id" :label="drug.drugName" :value="drug.id" />
      </el-select>
      <el-button :loading="loading" @click="fetchEvidence">刷新</el-button>
      <span v-if="selectedDrug" class="drug-id">数据库药品 ID：{{ selectedDrug.id }}</span>
    </div>
    <el-descriptions v-if="overview" :column="2" border class="source-summary">
      <el-descriptions-item label="来源药品">{{ selectedDrug?.drugName || overview.compound_name }}</el-descriptions-item>
      <el-descriptions-item label="FRDB 化合物 ID">{{ overview.external_compound_id }}</el-descriptions-item>
      <el-descriptions-item label="数据版本">{{ showValue(overview.dataset_version) }}</el-descriptions-item>
      <el-descriptions-item label="原始记录数">{{ total }}</el-descriptions-item>
      <el-descriptions-item label="数据来源" :span="2">
        <el-link v-if="sourceUrl(overview.frdb_source_url)" :href="sourceUrl(overview.frdb_source_url)" target="_blank" rel="noopener noreferrer">
          {{ overview.source_name || overview.frdb_source_url }}
        </el-link>
        <span v-else>{{ showValue(overview.source_name) }}</span>
      </el-descriptions-item>
    </el-descriptions>
    <el-table v-if="isRisk" :data="items" v-loading="loading" border style="width: 100%">
      <el-table-column prop="frdb_ddi_id" label="FRDB 记录 ID" width="130" />
      <el-table-column prop="ddi_target" label="原始靶点" min-width="170" show-overflow-tooltip />
      <el-table-column label="原始关系" min-width="230" show-overflow-tooltip>
        <template #default="{ row }">{{ formatSourceTerm(row.ddi_relation, 'ddiRelation') }}</template>
      </el-table-column>
      <el-table-column label="类型" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ formatSourceTerm(row.ddi_type, 'ddiType') }}</template>
      </el-table-column>
      <el-table-column label="原始证据" min-width="300" show-overflow-tooltip>
        <template #default="{ row }">{{ formatSourceTerm(row.ddi_clin_evidence, 'ddiEvidence') }}</template>
      </el-table-column>
      <el-table-column label="来源链接" min-width="200">
        <template #default="{ row }">
          <el-link v-if="sourceUrl(row.ddi_url)" :href="sourceUrl(row.ddi_url)" target="_blank" rel="noopener noreferrer">查看来源</el-link>
          <span v-else>{{ showValue(row.ddi_url) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-table v-else :data="items" v-loading="loading" border style="width: 100%">
      <el-table-column prop="frdb_adverse_event_id" label="FRDB 记录 ID" width="130" />
      <el-table-column label="原始事件类型" min-width="270" show-overflow-tooltip>
        <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_type, 'adverseType') }}</template>
      </el-table-column>
      <el-table-column label="原始严重程度" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_severity, 'adverseSeverity') }}</template>
      </el-table-column>
      <el-table-column label="发生频率" min-width="140">
        <template #default="{ row }">{{ showValue(formatSourceTerm(row.adverseevents_frequency, 'adverseFrequency')) }} {{ row.adverseevents_frequency_units || '' }}</template>
      </el-table-column>
      <el-table-column prop="adverseevents_comment" label="原始说明（来源原文）" min-width="280" show-overflow-tooltip />
      <el-table-column label="来源链接" min-width="200">
        <template #default="{ row }">
          <el-link v-if="sourceUrl(row.toxicity_source_uri)" :href="sourceUrl(row.toxicity_source_uri)" target="_blank" rel="noopener noreferrer">查看来源</el-link>
          <span v-else>{{ showValue(row.toxicity_source_uri) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-bar">
      <el-pagination
        :current-page="page" :page-size="size" :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="changeSize" @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin: 20px 0; }
.drug-id { color: #606266; }
.source-summary { margin-bottom: 20px; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
