import request from '../utils/request'

export function getDrugList() {
  return request({
    url: '/drug/page',
    method: 'get',
    params: { page: 1, size: 1 }
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
