import request from '../utils/request'

export function getDrugRiskList() {
  return request({
    url: '/drug-risk/list',
    method: 'get'
  })
}

export function getDrugRiskById(id) {
  return request({
    url: `/drug-risk/${id}`,
    method: 'get'
  })
}

export function createDrugRisk(data) {
  return request({
    url: '/drug-risk',
    method: 'post',
    data
  })
}

export function updateDrugRisk(id, data) {
  return request({
    url: `/drug-risk/${id}`,
    method: 'put',
    data
  })
}

export function deleteDrugRisk(id) {
  return request({
    url: `/drug-risk/${id}`,
    method: 'delete'
  })
}
