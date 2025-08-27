import request from '@/utils/request'

// 查询卡密列表
export function listCard(query) {
  return request({
    url: '/script/card/list',
    method: 'get',
    params: query
  })
}

// 查询卡密详细
export function getCard(id) {
  return request({
    url: '/script/card/' + id,
    method: 'get'
  })
}

// 根据卡密号查询卡密信息
export function getCardByCardNo(cardNo) {
  return request({
    url: '/script/card/getByCardNo/' + cardNo,
    method: 'get'
  })
}

// 新增卡密
export function addCard(data) {
  return request({
    url: '/script/card',
    method: 'post',
    data: data
  })
}

// 修改卡密
export function updateCard(data) {
  return request({
    url: '/script/card',
    method: 'put',
    data: data
  })
}

// 删除卡密
export function delCard(id) {
  return request({
    url: '/script/card/' + id,
    method: 'delete'
  })
}