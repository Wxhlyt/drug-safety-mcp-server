<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../../store'
import {
  getAdverseReactionList,
  createAdverseReaction,
  updateAdverseReaction,
  deleteAdverseReaction
} from '../../api/adverseReaction'
import { getDrugList } from '../../api/drug'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))

const loading = ref(false)
const adverseList = ref([])
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
  reactionName: '',
  reactionDescription: '',
  severityLevel: '',
  occurrenceTime: null,
  reporter: '',
  treatment: ''
})

const formRef = ref()

const severityOptions = [
  { label: '轻度', value: 'LOW' },
  { label: '中度', value: 'MEDIUM' },
  { label: '重度', value: 'HIGH' }
]

const rules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  reactionName: [{ required: true, message: '请输入不良反应名称', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    drugId: null,
    reactionName: '',
    reactionDescription: '',
    severityLevel: '',
    occurrenceTime: null,
    reporter: '',
    treatment: ''
  })
  currentId.value = null
}

const getDrugName = (drugId) => {
  const drug = drugList.value.find(item => item.id === drugId)
  return drug ? drug.drugName : '-'
}

const getSeverityTag = (level) => {
  const map = {
    LOW: { type: 'success', label: '轻度' },
    MEDIUM: { type: 'warning', label: '中度' },
    HIGH: { type: 'danger', label: '重度' }
  }
  return map[level] || { type: 'info', label: level }
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
    const res = await getAdverseReactionList()
    if (res.code === 200) {
      adverseList.value = res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const filteredAdverseList = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return adverseList.value
  return adverseList.value.filter(item => {
    const drugName = getDrugName(item.drugId).toLowerCase()
    return drugName.includes(keyword) ||
      (item.reactionName && item.reactionName.toLowerCase().includes(keyword)) ||
      (item.reporter && item.reporter.toLowerCase().includes(keyword))
  })
})

const total = computed(() => filteredAdverseList.value.length)

const paginatedAdverseList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAdverseList.value.slice(start, end)
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
  dialogTitle.value = '新增不良反应记录'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑不良反应记录'
  currentId.value = row.id
  Object.assign(form, {
    drugId: row.drugId,
    reactionName: row.reactionName,
    reactionDescription: row.reactionDescription || '',
    severityLevel: row.severityLevel || '',
    occurrenceTime: row.occurrenceTime ? new Date(row.occurrenceTime) : null,
    reporter: row.reporter || '',
    treatment: row.treatment || ''
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
      occurrenceTime: form.occurrenceTime || null
    }

    let res
    if (isEdit.value) {
      res = await updateAdverseReaction(currentId.value, submitData)
    } else {
      res = await createAdverseReaction(submitData)
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
    `确定删除该不良反应记录吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await deleteAdverseReaction(row.id)
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
  fetchDrugList()
  fetchList()
})
</script>

<template>
  <div class="adverse-container">
    <div class="page-header">
      <h2>不良反应管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="handleAdd">新增不良反应记录</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="请输入药品名称、症状或报告人搜索"
        clearable
        style="width: 360px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="paginatedAdverseList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="药品名称" min-width="150">
        <template #default="{ row }">
          {{ getDrugName(row.drugId) }}
        </template>
      </el-table-column>
      <el-table-column prop="reactionName" label="症状" min-width="150" />
      <el-table-column prop="severityLevel" label="严重程度" width="100">
        <template #default="{ row }">
          <el-tag :type="getSeverityTag(row.severityLevel).type">
            {{ getSeverityTag(row.severityLevel).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="occurrenceTime" label="发生时间" min-width="160" />
      <el-table-column prop="reporter" label="报告人" min-width="120" />
      <el-table-column prop="treatment" label="处理措施" min-width="180" show-overflow-tooltip />
      <el-table-column prop="reactionDescription" label="不良反应描述" min-width="200" show-overflow-tooltip />
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
        <el-form-item label="症状" prop="reactionName">
          <el-input v-model="form.reactionName" placeholder="请输入不良反应症状" />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="form.severityLevel" placeholder="请选择严重程度" style="width: 100%">
            <el-option
              v-for="item in severityOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker
            v-model="form.occurrenceTime"
            type="datetime"
            placeholder="请选择发生时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报告人">
          <el-input v-model="form.reporter" placeholder="请输入报告人" />
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input
            v-model="form.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入处理措施"
          />
        </el-form-item>
        <el-form-item label="不良反应描述">
          <el-input
            v-model="form.reactionDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入不良反应描述"
          />
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
.adverse-container {
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
