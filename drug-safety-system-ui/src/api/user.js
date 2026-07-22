import request from '../utils/request'

export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function getUserRoles(id) {
  return request({
    url: `/user/${id}/roles`,
    method: 'get'
  })
}

export function assignUserRoles(userId, roleIds) {
  console.log('assignUserRoles API 调用:', { userId, roleIds })
  return request({
    url: '/user/assignRoles',
    method: 'post',
    data: {
      userId,
      roleIds: roleIds || []
    }
  })
}
