import request from '@/utils/request'

// 查询脚本版本列表
export function listVersion(query) {
  return request({
    url: '/script/version/list',
    method: 'get',
    params: query
  })
}

// 查询脚本版本详细
export function getVersion(id) {
  return request({
    url: '/script/version/' + id,
    method: 'get'
  })
}

// 根据游戏ID获取脚本版本列表
export function getVersionsByGameId(gameId) {
  return request({
    url: '/script/version/game/' + gameId,
    method: 'get'
  })
}

// 获取指定游戏的最新版本号
export function getMaxVersionByGameId(gameId) {
  return request({
    url: '/script/version/maxVersion/' + gameId,
    method: 'get'
  })
}

// 新增脚本版本
export function addVersion(data) {
  return request({
    url: '/script/version',
    method: 'post',
    data: data
  })
}

// 修改脚本版本
export function updateVersion(data) {
  return request({
    url: '/script/version',
    method: 'put',
    data: data
  })
}

// 删除脚本版本
export function delVersion(id) {
  return request({
    url: '/script/version/' + id,
    method: 'delete'
  })
}