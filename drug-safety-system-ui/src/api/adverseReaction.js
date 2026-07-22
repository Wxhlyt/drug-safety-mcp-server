import request from '../utils/request'

export function getAdverseReactionList() {
  return request({
    url: '/adverse-reaction/list',
    method: 'get'
  })
}

export function getAdverseReactionById(id) {
  return request({
    url: `/adverse-reaction/${id}`,
    method: 'get'
  })
}

export function createAdverseReaction(data) {
  return request({
    url: '/adverse-reaction',
    method: 'post',
    data
  })
}

export function updateAdverseReaction(id, data) {
  return request({
    url: `/adverse-reaction/${id}`,
    method: 'put',
    data
  })
}

export function deleteAdverseReaction(id) {
  return request({
    url: `/adverse-reaction/${id}`,
    method: 'delete'
  })
}
