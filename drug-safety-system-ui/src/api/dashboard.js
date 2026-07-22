import request from '../utils/request'

export function getDrugList() {
  return request({
    url: '/drug/list',
    method: 'get'
  })
}

export function getDrugRiskList() {
  return request({
    url: '/drug-risk/list',
    method: 'get'
  })
}

export function getAdverseReactionList() {
  return request({
    url: '/adverse-reaction/list',
    method: 'get'
  })
}

export function getAiRiskList() {
  return request({
    url: '/ai-risk/list',
    method: 'get'
  })
}
