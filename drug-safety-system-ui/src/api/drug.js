import request from '../utils/request'

export function getDrugList() {
  return request({
    url: '/drug/list',
    method: 'get'
  })
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
