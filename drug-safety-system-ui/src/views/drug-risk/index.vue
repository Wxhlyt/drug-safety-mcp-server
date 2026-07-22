<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../../store'
import {
  getDrugRiskList,
  createDrugRisk,
  updateDrugRisk,
  deleteDrugRisk
} from '../../api/drugRisk'
import { getDrugList } from '../../api/drug'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))

const loading = ref(false)
const drugRiskList = ref([])
const drugList = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  drugId: null,
  riskLevel: '',
  riskScore: null,
  riskReason: '',
  analysisResult: '',
  analysisType: 'RULE'
})

const formRef = ref()

const riskLevelOptions = [
  { label: '低风险', value: 'LOW' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '高风险', value: 'HIGH' },
  { label: '未知', value: 'UNKNOWN' }
]

const analysisTypeOptions = [
  { label: '规则分析', value: 'RULE' },
  { label: 'AI辅助分析', value: 'AI' }
]

const rules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, {
    drugId: null,
    riskLevel: '',
    riskScore: null,
    riskReason: '',
    analysisResult: '',
    analysisType: 'RULE'
  })
  currentId.value = null
}

const getDrugName = (drugId) => {
  const drug = drugList.value.find(item => item.id === drugId)
  return drug ? drug.drugName : '-'
}

const fetchDrugList = async () => {
  try {
    const res = await getDrugList()
    if (res.code === 200) {
      drugList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载药品列表失败')
    }
  } catch (error) {
    ElMessage.error('加载药品列表失败')
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getDrugRiskList()
    if (res.code === 200) {
      drugRiskList.value = res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const filteredDrugRiskList = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return drugRiskList.value
  return drugRiskList.value.filter(item => {
    const drugName = getDrugName(item.drugId).toLowerCase()
    return drugName.includes(keyword) ||
      (item.riskLevel && item.riskLevel.toLowerCase().includes(keyword)) ||
      (item.riskReason && item.riskReason.toLowerCase().includes(keyword))
  })
})

const total = computed(() => filteredDrugRiskList.value.length)

const paginatedDrugRiskList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredDrugRiskList.value.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增风险记录'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑风险记录'
  currentId.value = row.id
  Object.assign(form, {
    drugId: row.drugId,
    riskLevel: row.riskLevel,
    riskScore: row.riskScore,
    riskReason: row.riskReason || '',
    analysisResult: row.analysisResult || '',
    analysisType: row.analysisType || 'RULE'
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
    const submitData = {
      ...form,
      riskScore: form.riskScore ? parseFloat(form.riskScore) : null
    }

    let res
    if (isEdit.value) {
      res = await updateDrugRisk(currentId.value, submitData)
    } else {
      res = await createDrugRisk(submitData)
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
    `确定删除该风险记录吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await deleteDrugRisk(row.id)
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

const getRiskLevelTag = (level) => {
  const map = {
    LOW: { type: 'success', label: '低风险' },
    MEDIUM: { type: 'warning', label: '中风险' },
    HIGH: { type: 'danger', label: '高风险' },
    UNKNOWN: { type: 'info', label: '未知' }
  }
  return map[level] || { type: 'info', label: level }
}

onMounted(() => {
  fetchDrugList()
  fetchList()
})
</script>

<template>
  <div class="drug-risk-container">
    <div class="page-header">
      <h2>药品风险管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="handleAdd">新增风险记录</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="请输入药品名称、风险等级或原因搜索"
        clearable
        style="width: 360px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="paginatedDrugRiskList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="药品名称" min-width="150">
        <template #default="{ row }">
          {{ getDrugName(row.drugId) }}
        </template>
      </el-table-column>
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="getRiskLevelTag(row.riskLevel).type">
            {{ getRiskLevelTag(row.riskLevel).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riskScore" label="风险评分" width="100" />
      <el-table-column prop="riskReason" label="风险原因" min-width="180" show-overflow-tooltip />
      <el-table-column prop="analysisResult" label="分析结果" min-width="180" show-overflow-tooltip />
      <el-table-column prop="analysisType" label="分析类型" width="120">
        <template #default="{ row }">
          {{ row.analysisType === 'AI' ? 'AI辅助分析' : '规则分析' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="isAdmin" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="isAdmin" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" style="width: 100%">
            <el-option
              v-for="item in drugList"
              :key="item.id"
              :label="item.drugName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="form.riskLevel" placeholder="请选择风险等级" style="width: 100%">
            <el-option
              v-for="item in riskLevelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="风险评分">
          <el-input-number
            v-model="form.riskScore"
            :min="0"
            :max="100"
            :precision="2"
            style="width: 100%"
            placeholder="请输入风险评分"
          />
        </el-form-item>
        <el-form-item label="风险原因">
          <el-input
            v-model="form.riskReason"
            placeholder="请输入风险原因"
          />
        </el-form-item>
        <el-form-item label="风险描述">
          <el-input
            v-model="form.analysisResult"
            type="textarea"
            :rows="3"
            placeholder="请输入风险分析结果或描述"
          />
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select v-model="form.analysisType" placeholder="请选择分析类型" style="width: 100%">
            <el-option
              v-for="item in analysisTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.drug-risk-container {
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
</style>
