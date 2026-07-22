import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/login/index.vue'
import Layout from '../layouts/Layout.vue'
import Home from '../views/home/index.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: Home
      },
      {
        path: 'drug',
        name: 'Drug',
        component: () => import('../views/drug/index.vue')
      },
      {
        path: 'drug-risk',
        name: 'DrugRisk',
        component: () => import('../views/drug-risk/index.vue')
      },
      {
        path: 'adverse',
        name: 'Adverse',
        component: () => import('../views/adverse/index.vue')
      },
      {
        path: 'ai-risk',
        name: 'AiRisk',
        meta: { roles: ['ADMIN'] },
        component: () => import('../views/ai-risk/index.vue')
      },
      {
        path: 'user',
        name: 'User',
        meta: { roles: ['ADMIN'] },
        component: () => import('../views/user/index.vue')
      },
      {
        path: 'role',
        name: 'Role',
        meta: { roles: ['ADMIN'] },
        component: () => import('../views/role/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  let userInfo = {}
  try {
    userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}
  } catch (e) {
    console.error('解析 userInfo 失败', e)
    userInfo = {}
  }
  const userRoles = userInfo.roles || []

  if (to.path === '/login') {
    return true
  }

  if (to.matched.some(record => record.meta.requiresAuth) && !token) {
    return '/login'
  }

  const requiredRoles = to.matched.find(record => record.meta.roles)?.meta.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = requiredRoles.some(role => userRoles.includes(role))
    if (!hasRole) {
      return '/home'
    }
  }

  return true
})

export default router
