import request from '@/utils/request'

// 查询脚本日志列表
export function listLog(query) {
  return request({
    url: '/script/log/list',
    method: 'get',
    params: query
  })
}

// 查询脚本日志详细
export function getLog(id) {
  return request({
    url: '/script/log/' + id,
    method: 'get'
  })
}

// 根据设备ID获取脚本日志列表
export function getLogsByAndroidId(androidId) {
  return request({
    url: '/script/log/android/' + androidId,
    method: 'get'
  })
}

// 根据日志级别获取脚本日志列表
export function getLogsByLevel(level) {
  return request({
    url: '/script/log/level/' + level,
    method: 'get'
  })
}

// 新增脚本日志
export function addLog(data) {
  return request({
    url: '/script/log',
    method: 'post',
    data: data
  })
}

// 修改脚本日志
export function updateLog(data) {
  return request({
    url: '/script/log',
    method: 'put',
    data: data
  })
}

// 删除脚本日志
export function delLog(id) {
  return request({
    url: '/script/log/' + id,
    method: 'delete'
  })
}

// 清空指定设备的日志
export function clearLogsByAndroidId(androidId) {
  return request({
    url: '/script/log/android/' + androidId,
    method: 'delete'
  })
}

// 清空指定日期之前的日志
export function clearLogsBeforeDate(beforeDate) {
  return request({
    url: '/script/log/before',
    method: 'delete',
    params: { beforeDate }
  })
}

// 批量上报脚本日志
export function batchAddLogs(data) {
  return request({
    url: '/script/log/batch',
    method: 'post',
    data: data
  })
}