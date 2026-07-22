<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../../store'
import { getDrugList, createDrug, updateDrug, deleteDrug } from '../../api/drug'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))

const loading = ref(false)
const drugList = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

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
    const res = await getDrugList()
    if (res.code === 200) {
      drugList.value = res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const filteredDrugList = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return drugList.value
  return drugList.value.filter(drug =>
    (drug.drugName && drug.drugName.toLowerCase().includes(keyword)) ||
    (drug.genericName && drug.genericName.toLowerCase().includes(keyword))
  )
})

const total = computed(() => filteredDrugList.value.length)

const paginatedDrugList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredDrugList.value.slice(start, end)
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
    </div>

    <el-table :data="paginatedDrugList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="drugName" label="药品名称" min-width="150" />
      <el-table-column prop="genericName" label="通用名称" min-width="150" />
      <el-table-column prop="drugCategory" label="药品分类" min-width="120" />
      <el-table-column prop="categoryCode" label="分类编码" min-width="120" />
      <el-table-column prop="dosageForm" label="剂型" min-width="120" />
      <el-table-column prop="specification" label="规格" min-width="150" />
      <el-table-column prop="manufacturer" label="生产厂家" min-width="150" />
      <el-table-column prop="approvalNumber" label="批准文号" min-width="150" />
      <el-table-column prop="riskLevel" label="风险等级" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
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
</style>
