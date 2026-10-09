<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../../store'
import { getDrugPage, createDrug, updateDrug, deleteDrug, getExternalDrugOverview, getExternalDrugDdi, getExternalDrugAdverse } from '../../api/drug'
import { formatSourceTerm } from '../../utils/sourceTranslations'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))

const loading = ref(false)
const drugList = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sourceFilter = ref('')
const qualityFilter = ref('')

const evidenceVisible = ref(false)
const evidenceLoading = ref(false)
const externalEvidence = ref(null)
const externalDrugId = ref(null)
const evidencePageSize = 10
const ddiPage = ref(1)
const adversePage = ref(1)
const ddiLoading = ref(false)
const adverseLoading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  drugName: '',
  genericName: '',
  drugCategory: '',
  categoryCode: '',
  dosageForm: '',
  specification: '',
  storageCondition: '',
  manufacturer: '',
  approvalNumber: '',
  drugDescription: ''
})

const formRef = ref()

const rules = {
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  genericName: [{ required: true, message: '请输入通用名称', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    drugName: '',
    genericName: '',
    drugCategory: '',
    categoryCode: '',
    dosageForm: '',
    specification: '',
    storageCondition: '',
    manufacturer: '',
    approvalNumber: '',
    drugDescription: ''
  })
  currentId.value = null
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getDrugPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value.trim() || undefined,
      source: sourceFilter.value || undefined,
      quality: qualityFilter.value || undefined
    })
    if (res.code === 200) {
      drugList.value = res.data?.items || []
      total.value = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const getQualityLabel = (row) => {
  if (row.dataQualityStatus === 'FRDB_SOURCE_ONLY') return 'FRDB 基础数据'
  return row.dataQualityStatus || '本地维护'
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchList()
}

const openExternalEvidence = async (row) => {
  evidenceVisible.value = true
  evidenceLoading.value = true
  externalEvidence.value = null
  externalDrugId.value = row.id
  ddiPage.value = 1
  adversePage.value = 1
  try {
    const [overview, ddi, adverse] = await Promise.all([
      getExternalDrugOverview(row.id),
      getExternalDrugDdi(row.id, { page: 1, size: 10 }),
      getExternalDrugAdverse(row.id, { page: 1, size: 10 })
    ])
    externalEvidence.value = { overview: overview.data, ddi: ddi.data, adverse: adverse.data }
  } catch (error) {
    evidenceVisible.value = false
  } finally {
    evidenceLoading.value = false
  }
}

const loadDdiPage = async (page) => {
  if (!externalDrugId.value || !externalEvidence.value) return
  ddiLoading.value = true
  try {
    const res = await getExternalDrugDdi(externalDrugId.value, { page, size: evidencePageSize })
    externalEvidence.value.ddi = res.data
    ddiPage.value = res.data.page
  } finally {
    ddiLoading.value = false
  }
}

const loadAdversePage = async (page) => {
  if (!externalDrugId.value || !externalEvidence.value) return
  adverseLoading.value = true
  try {
    const res = await getExternalDrugAdverse(externalDrugId.value, { page, size: evidencePageSize })
    externalEvidence.value.adverse = res.data
    adversePage.value = res.data.page
  } finally {
    adverseLoading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增药品'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑药品'
  currentId.value = row.id
  Object.assign(form, {
    drugName: row.drugName,
    genericName: row.genericName,
    drugCategory: row.drugCategory || '',
    categoryCode: row.categoryCode || '',
    dosageForm: row.dosageForm || '',
    specification: row.specification || '',
    storageCondition: row.storageCondition || '',
    manufacturer: row.manufacturer || '',
    approvalNumber: row.approvalNumber || '',
    drugDescription: row.drugDescription || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    let res
    if (isEdit.value) {
      res = await updateDrug(currentId.value, form)
    } else {
      res = await createDrug(form)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除药品【${row.drugName}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await deleteDrug(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          fetchList()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        ElMessage.error('请求失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="drug-container">
    <div class="page-header">
      <h2>药品信息管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="handleAdd">新增药品</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="请输入药品名称或通用名称搜索"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-select v-model="sourceFilter" clearable placeholder="数据来源" style="width: 130px" @change="handleSearch">
        <el-option label="FRDB" value="FRDB" />
        <el-option label="本地" value="LOCAL" />
      </el-select>
      <el-select v-model="qualityFilter" clearable placeholder="完整度" style="width: 160px" @change="handleSearch">
        <el-option label="FRDB 基础数据" value="FRDB_SOURCE_ONLY" />
      </el-select>
    </div>

    <el-table :data="drugList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="drugName" label="药品名称" min-width="220" />
      <el-table-column label="通用名 / UNII" min-width="240">
        <template #default="{ row }">
          <div>{{ row.genericName || '未提供' }}</div>
          <small v-if="row.externalUnii">UNII: {{ row.externalUnii }}</small>
        </template>
      </el-table-column>
      <el-table-column label="数据来源" width="120">
        <template #default="{ row }">
          <el-tag :type="row.dataSource === 'FRDB' ? 'info' : 'success'">{{ row.dataSource || 'LOCAL' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数据完整度" min-width="150">
        <template #default="{ row }">
          <el-tag :type="row.dataQualityStatus === 'FRDB_SOURCE_ONLY' ? 'warning' : 'success'">{{ getQualityLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.dataSource === 'FRDB'" type="primary" size="small" @click="openExternalEvidence(row)">查看详情</el-button>
          <el-button v-if="isAdmin && !row.sourceReadOnly" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="isAdmin && !row.sourceReadOnly" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="通用名称" prop="genericName">
          <el-input v-model="form.genericName" placeholder="请输入通用名称" />
        </el-form-item>
        <el-form-item label="药品分类">
          <el-input v-model="form.drugCategory" placeholder="请输入药品分类" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="剂型">
          <el-input v-model="form.dosageForm" placeholder="请输入剂型" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="储存条件">
          <el-input v-model="form.storageCondition" placeholder="请输入储存条件" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="批准文号">
          <el-input v-model="form.approvalNumber" placeholder="请输入批准文号" />
        </el-form-item>
        <el-form-item label="药品描述">
          <el-input
            v-model="form.drugDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入药品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="evidenceVisible" title="外部来源与原始证据（只读）" size="65%" destroy-on-close>
      <div v-loading="evidenceLoading" v-if="externalEvidence">
        <el-alert type="info" :closable="false" show-icon title="FRDB 原始数据只读展示；DailyMed 仅在唯一高置信匹配时显示标签版本。" />
        <el-descriptions :column="2" border class="evidence-block">
          <el-descriptions-item label="FRDB 化合物 ID">{{ externalEvidence.overview.external_compound_id }}</el-descriptions-item>
          <el-descriptions-item label="UNII">{{ externalEvidence.overview.external_unii || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="映射状态">{{ formatSourceTerm(externalEvidence.overview.mapping_status, 'mappingStatus') }}</el-descriptions-item>
          <el-descriptions-item label="映射方法">{{ formatSourceTerm(externalEvidence.overview.match_method, 'matchMethod') }}</el-descriptions-item>
          <el-descriptions-item label="FRDB 版本">{{ externalEvidence.overview.dataset_version }}</el-descriptions-item>
          <el-descriptions-item label="数据质量">{{ formatSourceTerm(externalEvidence.overview.data_quality_status, 'dataQuality') }}</el-descriptions-item>
          <el-descriptions-item label="FRDB 来源" :span="2"><el-link :href="externalEvidence.overview.frdb_source_url" target="_blank">{{ externalEvidence.overview.frdb_source_url }}</el-link></el-descriptions-item>
          <el-descriptions-item v-if="externalEvidence.overview.dailymedLabel" label="DailyMed 标签版本" :span="2">
            {{ externalEvidence.overview.dailymedLabel.spl_set_id }} / {{ externalEvidence.overview.dailymedLabel.spl_version }}
            <el-link :href="externalEvidence.overview.dailymedLabel.source_url" target="_blank" class="source-link">官方原始 XML</el-link>
          </el-descriptions-item>
        </el-descriptions>
        <template v-if="externalEvidence.overview.dailymedProducts?.length">
          <h3>DailyMed 结构化产品信息</h3>
          <el-table :data="externalEvidence.overview.dailymedProducts" border max-height="220">
            <el-table-column prop="product_identifier" label="产品标识" min-width="130" />
            <el-table-column prop="product_name" label="产品名称" min-width="170" />
            <el-table-column prop="active_ingredients" label="活性成分" min-width="180" />
            <el-table-column label="剂型" min-width="230" show-overflow-tooltip>
              <template #default="{ row }">{{ formatSourceTerm(row.dosage_form, 'dosageForm') }}</template>
            </el-table-column>
            <el-table-column prop="strength_text" label="规格" min-width="130" />
            <el-table-column prop="route_text" label="给药途径" min-width="120" />
          </el-table>
        </template>
        <template v-if="externalEvidence.overview.dailymedSections?.length">
          <h3>DailyMed 标签章节（原始文本节选）</h3>
          <el-collapse>
            <el-collapse-item v-for="section in externalEvidence.overview.dailymedSections" :key="section.section_sha256" :title="section.section_title">
              <p class="section-text">{{ section.section_text }}</p>
            </el-collapse-item>
          </el-collapse>
        </template>
        <h3>FRDB 相互作用证据（共 {{ externalEvidence.ddi.total }} 条）</h3>
        <el-table :data="externalEvidence.ddi.items" border max-height="240" v-loading="ddiLoading">
          <el-table-column prop="ddi_target" label="靶点" min-width="160" />
          <el-table-column label="关系" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">{{ formatSourceTerm(row.ddi_relation, 'ddiRelation') }}</template>
          </el-table-column>
          <el-table-column label="类型" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ formatSourceTerm(row.ddi_type, 'ddiType') }}</template>
          </el-table-column>
          <el-table-column label="临床证据" min-width="300" show-overflow-tooltip>
            <template #default="{ row }">{{ formatSourceTerm(row.ddi_clin_evidence, 'ddiEvidence') }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="externalEvidence.ddi.total > evidencePageSize"
          small background layout="total, prev, pager, next"
          :current-page="ddiPage" :page-size="evidencePageSize" :total="externalEvidence.ddi.total"
          @current-change="loadDdiPage"
        />
        <h3>FRDB 不良反应证据（共 {{ externalEvidence.adverse.total }} 条）</h3>
        <el-table :data="externalEvidence.adverse.items" border max-height="240" v-loading="adverseLoading">
          <el-table-column label="严重程度" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_severity, 'adverseSeverity') }}</template>
          </el-table-column>
          <el-table-column label="类型" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">{{ formatSourceTerm(row.adverseevents_type, 'adverseType') }}</template>
          </el-table-column>
          <el-table-column prop="adverseevents_comment" label="原始说明（来源原文）" min-width="260" show-overflow-tooltip />
          <el-table-column prop="toxicity_source_uri" label="来源 URI" min-width="220" show-overflow-tooltip />
        </el-table>
        <el-pagination
          v-if="externalEvidence.adverse.total > evidencePageSize"
          small background layout="total, prev, pager, next"
          :current-page="adversePage" :page-size="evidencePageSize" :total="externalEvidence.adverse.total"
          @current-change="loadAdversePage"
        />
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.drug-container {
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

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.evidence-block { margin-top: 16px; }
.source-link { margin-left: 12px; }
.section-text { white-space: pre-wrap; line-height: 1.7; }
</style>
