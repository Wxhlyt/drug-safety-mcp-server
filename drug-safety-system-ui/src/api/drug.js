import request from '../utils/request'

export function getDrugList() {
  return request({
    url: '/drug/list',
    method: 'get'
  })
}

export function getDrugPage(params) {
  return request({
    url: '/drug/page',
    method: 'get',
    params
  })
}

export function getDrugOptions(keyword) {
  return request({
    url: '/drug/options',
    method: 'get',
    params: { keyword: keyword || undefined }
  })
}

export function getExternalDrugOverview(id) {
  return request({ url: `/external-drug/${id}/overview`, method: 'get' })
}

export function getExternalDrugDdi(id, params) {
  return request({ url: `/external-drug/${id}/ddi`, method: 'get', params })
}

export function getExternalDrugAdverse(id, params) {
  return request({ url: `/external-drug/${id}/adverse`, method: 'get', params })
}

export function getDrugById(id) {
  return request({
    url: `/drug/${id}`,
    method: 'get'
  })
}

export function createDrug(data) {
  return request({
    url: '/drug',
    method: 'post',
    data
  })
}

export function updateDrug(id, data) {
  return request({
    url: `/drug/${id}`,
    method: 'put',
    data
  })
}

export function deleteDrug(id) {
  return request({
    url: `/drug/${id}`,
    method: 'delete'
  })
}
