<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { FirstAidKit, Warning, WarningFilled, Cpu } from '@element-plus/icons-vue'
import { useAppStore } from '../../store'
import {
  getDrugList,
  getDrugRiskList,
  getAdverseReactionList,
  getAiRiskList
} from '../../api/dashboard'

const appStore = useAppStore()
const isAdmin = computed(() => (appStore.userInfo?.roles || []).includes('ADMIN'))
const loading = ref(false)

const stats = reactive({
  drugCount: 0,
  riskCount: 0,
  adverseCount: 0,
  aiRiskCount: 0
})

const riskLevels = reactive({
  high: 0,
  medium: 0,
  low: 0
})

const chartRef = ref()
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '药品风险等级分布',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['低风险', '中风险', '高风险'],
      axisLine: { lineStyle: { color: '#909399' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#909399' } },
      axisLabel: { color: '#606266' },
      splitLine: { lineStyle: { color: '#E4E7ED' } }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: [riskLevels.low, riskLevels.medium, riskLevels.high],
        itemStyle: {
          color: (params) => {
            const colors = ['#67C23A', '#E6A23C', '#F56C6C']
            return colors[params.dataIndex]
          },
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '50%'
      }
    ]
  }
  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption({
      series: [
        {
          data: [riskLevels.low, riskLevels.medium, riskLevels.high]
        }
      ]
    })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const requestList = [
      getDrugList(),
      getDrugRiskList(),
      getAdverseReactionList()
    ]
    if (isAdmin.value) {
      requestList.push(getAiRiskList())
    }

    const [drugRes, riskRes, adverseRes, aiRiskRes] = await Promise.all(requestList)

    if (drugRes.code === 200) {
      stats.drugCount = (drugRes.data || []).length
    }
    if (riskRes.code === 200) {
      const riskList = riskRes.data || []
      stats.riskCount = riskList.length
      riskLevels.high = riskList.filter(item => item.riskLevel === 'HIGH').length
      riskLevels.medium = riskList.filter(item => item.riskLevel === 'MEDIUM').length
      riskLevels.low = riskList.filter(item => item.riskLevel === 'LOW').length
    }
    if (adverseRes.code === 200) {
      stats.adverseCount = (adverseRes.data || []).length
    }
    if (isAdmin.value && aiRiskRes && aiRiskRes.code === 200) {
      stats.aiRiskCount = (aiRiskRes.data || []).length
    }

    await nextTick()
    if (!chartInstance) {
      initChart()
    } else {
      updateChart()
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  chartInstance && chartInstance.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<template>
  <div class="home-container" v-loading="loading">
    <div class="welcome-section">
      <h1 class="welcome-title">药品安全风险管理系统</h1>
      <p class="welcome-subtitle">基于智能医学工程的药品风险监测与分析平台</p>
      <p class="welcome-user">欢迎回来，{{ appStore.userInfo?.username || '用户' }}</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon drug-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.drugCount }}</div>
            <div class="stat-label">药品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon risk-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.riskCount }}</div>
            <div class="stat-label">风险记录数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon adverse-icon">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.adverseCount }}</div>
            <div class="stat-label">不良反应数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon ai-icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.aiRiskCount }}</div>
            <div class="stat-label">AI风险分析次数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="risk-level-row">
      <el-col :span="8">
        <el-card class="risk-card high-risk" shadow="hover">
          <div class="risk-title">高风险</div>
          <div class="risk-value">{{ riskLevels.high }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="risk-card medium-risk" shadow="hover">
          <div class="risk-title">中风险</div>
          <div class="risk-value">{{ riskLevels.medium }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="risk-card low-risk" shadow="hover">
          <div class="risk-title">低风险</div>
          <div class="risk-value">{{ riskLevels.low }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-container {
  min-height: calc(100vh - 140px);
}

.welcome-section {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.welcome-title {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: bold;
}

.welcome-subtitle {
  margin: 0 0 10px 0;
  font-size: 16px;
  opacity: 0.9;
}

.welcome-user {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 16px;
}

.drug-icon {
  background-color: #409EFF;
}

.risk-icon {
  background-color: #E6A23C;
}

.adverse-icon {
  background-color: #F56C6C;
}

.ai-icon {
  background-color: #67C23A;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.risk-level-row {
  margin-bottom: 20px;
}

.risk-card {
  text-align: center;
  padding: 20px;
}

.risk-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.risk-value {
  font-size: 36px;
  font-weight: bold;
}

.high-risk .risk-value {
  color: #F56C6C;
}

.medium-risk .risk-value {
  color: #E6A23C;
}

.low-risk .risk-value {
  color: #67C23A;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
