<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, FirstAidKit, Warning, WarningFilled, Cpu, User, UserFilled } from '@element-plus/icons-vue'
import { useAppStore } from '../store'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const iconMap = {
  House,
  FirstAidKit,
  Warning,
  WarningFilled,
  Cpu,
  User,
  UserFilled
}

const menuList = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/drug', title: '药品信息管理', icon: 'FirstAidKit' },
  { path: '/drug-risk', title: '药品风险管理', icon: 'Warning' },
  { path: '/adverse', title: '不良反应管理', icon: 'WarningFilled' },
  { path: '/ai-risk', title: '来源证据分析', icon: 'Cpu', roles: ['ADMIN'] },
  { path: '/user', title: '用户管理', icon: 'User', roles: ['ADMIN'] },
  { path: '/role', title: '角色管理', icon: 'UserFilled', roles: ['ADMIN'] }
]

const userRoles = computed(() => appStore.userInfo?.roles || [])

const hasMenuRole = (menu) => {
  if (!menu.roles || menu.roles.length === 0) {
    return true
  }
  return menu.roles.some(role => userRoles.value.includes(role))
}

const visibleMenuList = computed(() => menuList.filter(hasMenuRole))

const handleMenuClick = (path) => {
  router.push(path)
}

const handleLogout = () => {
  appStore.clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        药品安全系统
      </div>
      <el-menu
        :default-active="route.path"
        class="layout-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse-transition="false"
      >
        <el-menu-item
          v-for="item in visibleMenuList"
          :key="item.path"
          :index="item.path"
          @click="handleMenuClick(item.path)"
        >
          <el-icon>
            <component :is="iconMap[item.icon]" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">智能药品安全管理系统</div>
        <div class="header-right">
          <span class="username">{{ appStore.userInfo?.username || '用户' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.layout-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
  color: #fff;
}

.layout-menu {
  border-right: none;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #606266;
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
