import request from '@/utils/request'

// 查询游戏列表
export function listGame(query) {
  return request({
    url: '/script/game/list',
    method: 'get',
    params: query
  })
}

// 查询游戏详细
export function getGame(id) {
  return request({
    url: '/script/game/' + id,
    method: 'get'
  })
}

// 新增游戏
export function addGame(data) {
  return request({
    url: '/script/game',
    method: 'post',
    data: data
  })
}

// 修改游戏
export function updateGame(data) {
  return request({
    url: '/script/game',
    method: 'put',
    data: data
  })
}

// 删除游戏
export function delGame(id) {
  return request({
    url: '/script/game/' + id,
    method: 'delete'
  })
}

// 获取游戏选择框列表
export function getGameOptions() {
  return request({
    url: '/script/game/optionselect',
    method: 'get'
  })
}