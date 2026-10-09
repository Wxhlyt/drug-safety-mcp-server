import request from '../utils/request'

export function analyzeAiRisk(data) {
  return request({
    url: '/ai-risk/analyze',
    method: 'post',
    data
  })
}

export function analyzeSourceEvidence(drugId) {
  return request({
    url: `/ai-risk/${drugId}/source-analysis`,
    method: 'get'
  })
}

export function getAiRiskList() {
  return request({
    url: '/ai-risk/list',
    method: 'get'
  })
}
