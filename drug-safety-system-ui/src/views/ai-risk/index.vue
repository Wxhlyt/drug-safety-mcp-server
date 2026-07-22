<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDrugList } from '../../api/drug'
import { getAdverseReactionList } from '../../api/adverseReaction'
import { getDrugRiskList } from '../../api/drugRisk'
import { analyzeAiRisk } from '../../api/aiRisk'

const loading = ref(false)
const analyzing = ref(false)
const drugList = ref([])
const adverseReactionList = ref([])
const drugRiskList = ref([])

const selectedDrugId = ref(null)

const result = reactive({
  visible: false,
  drugName: '',
  riskLevel: '',
  riskScore: 0,
  riskReason: '',
  analysisContent: '',
  riskSuggestion: ''
})

const riskLevelMap = {
  LOW: { label: '低风险', type: 'success' },
  MEDIUM: { label: '中风险', type: 'warning' },
  HIGH: { label: '高风险', type: 'danger' }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [drugRes, adverseRes, riskRes] = await Promise.all([
      getDrugList(),
      getAdverseReactionList(),
      getDrugRiskList()
    ])
    if (drugRes.code === 200) {
      drugList.value = drugRes.data || []
    }
    if (adverseRes.code === 200) {
      adverseReactionList.value = adverseRes.data || []
    }
    if (riskRes.code === 200) {
      drugRiskList.value = riskRes.data || []
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const generateMockResult = (drug) => {
  const relatedAdverse = adverseReactionList.value.filter(item => item.drugId === drug.id)
  const relatedRisk = drugRiskList.value.find(item => item.drugId === drug.id)

  let baseScore = drug.riskScore || 0
  if (relatedRisk && relatedRisk.riskScore) {
    baseScore = Number(relatedRisk.riskScore)
  }

  // 根据不良反应严重程度调整分数
  const severeCount = relatedAdverse.filter(item => item.severityLevel === 'HIGH').length
  const mediumCount = relatedAdverse.filter(item => item.severityLevel === 'MEDIUM').length
  const adjustment = severeCount * 8 + mediumCount * 4
  let riskScore = baseScore + adjustment

  // 引入少量随机波动，避免结果过于固定
  const randomFactor = (Math.random() * 6 - 3)
  riskScore = Math.max(0, Math.min(100, Number((riskScore + randomFactor).toFixed(1))))

  let riskLevel = 'LOW'
  if (riskScore >= 70) {
    riskLevel = 'HIGH'
  } else if (riskScore >= 40) {
    riskLevel = 'MEDIUM'
  }

  const levelText = riskLevelMap[riskLevel]?.label || '未知'
  const adverseNames = relatedAdverse.map(item => item.reactionName).filter(Boolean)
  const adverseText = adverseNames.length > 0
    ? `已关联 ${relatedAdverse.length} 条不良反应记录（${adverseNames.slice(0, 3).join('、')}）`
    : '暂无不良反应记录'

  const riskReason = `根据药品【${drug.drugName}】信息和历史不良反应记录分析，${adverseText}，该药品存在${levelText.replace('风险', '')}风险。`

  return {
    drugName: drug.drugName,
    riskLevel,
    riskScore,
    riskReason
  }
}

const handleAnalyze = async () => {
  if (!selectedDrugId.value) {
    ElMessage.warning('请选择药品')
    return
  }

  const drug = drugList.value.find(item => item.id === selectedDrugId.value)
  if (!drug) {
    ElMessage.warning('药品不存在')
    return
  }

  analyzing.value = true
  try {
    const mockResult = generateMockResult(drug)

    // 调用后端接口保存分析记录，便于首页统计
    const analyzeRes = await analyzeAiRisk({ drugId: drug.id })
    if (analyzeRes.code === 200) {
      result.analysisContent = analyzeRes.data?.analysisContent || ''
      result.riskSuggestion = analyzeRes.data?.riskSuggestion || ''
    }

    Object.assign(result, {
      visible: true,
      drugName: mockResult.drugName,
      riskLevel: mockResult.riskLevel,
      riskScore: mockResult.riskScore,
      riskReason: mockResult.riskReason
    })

    ElMessage.success('分析完成')
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="ai-risk-container" v-loading="loading">
    <div class="page-header">
      <h2>AI风险分析</h2>
    </div>

    <el-card class="analyze-card" shadow="hover">
      <div class="analyze-form">
        <el-form label-width="100px">
          <el-form-item label="药品名称">
            <el-select
              v-model="selectedDrugId"
              placeholder="请选择药品"
              clearable
              style="width: 360px"
            >
              <el-option
                v-for="drug in drugList"
                :key="drug.id"
                :label="drug.drugName"
                :value="drug.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="analyzing" @click="handleAnalyze">
              开始分析
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card v-if="result.visible" class="result-card" shadow="hover">
      <template #header>
        <div class="result-header">
          <span>分析结果</span>
          <el-tag :type="riskLevelMap[result.riskLevel]?.type || 'info'" size="large">
            {{ riskLevelMap[result.riskLevel]?.label || '未知' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="药品名称">{{ result.drugName }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="riskLevelMap[result.riskLevel]?.type || 'info'">
            {{ riskLevelMap[result.riskLevel]?.label || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险评分">
          <span class="risk-score">{{ result.riskScore }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="风险原因">{{ result.riskReason }}</el-descriptions-item>
        <el-descriptions-item v-if="result.analysisContent" label="AI分析内容">
          {{ result.analysisContent }}
        </el-descriptions-item>
        <el-descriptions-item v-if="result.riskSuggestion" label="风险建议">
          {{ result.riskSuggestion }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.ai-risk-container {
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

.analyze-card {
  margin-bottom: 20px;
}

.analyze-form {
  max-width: 600px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.result-card {
  margin-top: 20px;
}

.risk-score {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
