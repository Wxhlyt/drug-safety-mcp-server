import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const isAuthUrl = (url) => {
  return url && url.includes('/auth/')
}

const handleUnauthorized = () => {
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  window.location.href = '/login'
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const data = response.data
    const configUrl = response.config.url

    if (data.code === 401 && !isAuthUrl(configUrl)) {
      handleUnauthorized()
      return Promise.reject(new Error(data.message || '登录已过期'))
    }

    if (data.code === 403 && !isAuthUrl(configUrl)) {
      ElMessage.error(data.message || '权限不足')
      return Promise.reject(new Error(data.message || '权限不足'))
    }

    if (data.code !== 200) {
      if (!isAuthUrl(configUrl)) {
        ElMessage.error(data.message || '请求失败')
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }

    return data
  },
  (error) => {
    const { response, message, code } = error
    const configUrl = response?.config?.url

    // 登录接口 401 已在响应数据中处理，此处不再重复提示
    if (response?.status === 401 && !isAuthUrl(configUrl)) {
      handleUnauthorized()
      return Promise.reject(error)
    }

    let errorMessage = '网络请求失败，请稍后重试'
    if (code === 'ECONNABORTED' || message?.includes('timeout')) {
      errorMessage = '请求超时，请检查网络后重试'
    } else if (!response) {
      errorMessage = '网络连接失败，请检查网络后重试'
    } else if (response.status >= 500) {
      errorMessage = '服务器内部错误，请稍后重试'
    } else if (response?.data?.message) {
      errorMessage = response.data.message
    }

    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

export default request
