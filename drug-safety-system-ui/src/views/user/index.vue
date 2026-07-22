<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../../store'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  getUserRoles,
  assignUserRoles
} from '../../api/user'
import { getRoleList } from '../../api/role'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))

const loading = ref(false)
const userList = ref([])
const roleList = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref(null)
const selectedRoleIds = ref([])

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
})

const formRef = ref()

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    status: 1
  })
  currentId.value = null
  selectedRoleIds.value = []
}

const getStatusTag = (status) => {
  return status === 1
    ? { type: 'success', label: '启用' }
    : { type: 'danger', label: '禁用' }
}

const fetchUserRoles = async (user) => {
  try {
    const res = await getUserRoles(user.id)
    if (res.code === 200 && res.data) {
      user.roleNames = res.data.map(role => role.roleName).join('、') || '-'
      user.roleIds = res.data.map(role => role.id)
    } else {
      user.roleNames = '-'
      user.roleIds = []
    }
  } catch (error) {
    user.roleNames = '-'
    user.roleIds = []
  }
}

const fetchRoleList = async () => {
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    if (res.code === 200) {
      userList.value = (res.data || []).map(user => ({
        ...user,
        roleNames: '-',
        roleIds: []
      }))
      await Promise.all(userList.value.map(user => fetchUserRoles(user)))
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const filteredUserList = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return userList.value
  return userList.value.filter(user =>
    (user.username && user.username.toLowerCase().includes(keyword)) ||
    (user.email && user.email.toLowerCase().includes(keyword))
  )
})

const total = computed(() => filteredUserList.value.length)

const paginatedUserList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUserList.value.slice(start, end)
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
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  currentId.value = row.id
  selectedRoleIds.value = row.roleIds || []
  Object.assign(form, {
    username: row.username,
    password: '',
    realName: row.realName || '',
    phone: row.phone || '',
    email: row.email || '',
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  if (!isEdit.value && !form.password) {
    ElMessage.error('请输入密码')
    return
  }

  try {
    const submitData = {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      status: form.status
    }
    if (form.password) {
      submitData.password = form.password
    }

    let res
    if (isEdit.value) {
      res = await updateUser(currentId.value, submitData)
    } else {
      res = await createUser(submitData)
    }

    if (res.code === 200) {
      if (isEdit.value) {
        try {
          await assignUserRoles(currentId.value, selectedRoleIds.value)
        } catch (roleError) {
          ElMessage.warning('用户信息保存成功，但角色分配失败')
          dialogVisible.value = false
          fetchList()
          return
        }
      } else {
        console.log('=== 新增用户提交 ===', { isEdit: isEdit.value, form: form.username })
        console.log('createUser 响应:', res)
        console.log('createUser 响应 data:', res.data)
        try {
          let newUserId = res.data?.id
          if (!newUserId) {
            const listRes = await getUserList()
            if (listRes.code === 200 && listRes.data) {
              const newUser = listRes.data.find(u => u.username === form.username)
              newUserId = newUser?.id
            }
          }
          console.log('准备分配角色:', { newUserId, roleIds: selectedRoleIds.value })
          if (newUserId && selectedRoleIds.value.length > 0) {
            const roleRes = await assignUserRoles(newUserId, selectedRoleIds.value)
            console.log('assignUserRoles 调用结果:', roleRes)
          } else {
            console.log('无需分配角色或未能获取到新用户ID')
          }
        } catch (roleError) {
          console.error('角色分配异常:', roleError)
          ElMessage.warning('用户信息保存成功，但角色分配失败')
          dialogVisible.value = false
          fetchList()
          return
        }
      }
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
    `确定删除用户【${row.username}】吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const res = await deleteUser(row.id)
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
  fetchRoleList()
  fetchList()
})
</script>

<template>
  <div class="user-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="请输入用户名或邮箱搜索"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="paginatedUserList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          {{ row.roleNames || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status).type">
            {{ getStatusTag(row.status).label }}
          </el-tag>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="selectedRoleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleList"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
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
.user-container {
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
