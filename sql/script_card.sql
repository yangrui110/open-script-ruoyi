-- 创建卡密表
CREATE TABLE `script_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `card_no` varchar(20) NOT NULL COMMENT '卡密',
  `expire_day` int(5) NOT NULL COMMENT '过期天数',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `expire_time` varchar(10) DEFAULT NULL COMMENT '实际过期时间（初始为NULL，第一次绑定时赋值）',
  `device_size` varchar(50) DEFAULT '1' COMMENT '可绑定设备数',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_no` (`card_no`)
) COMMENT='卡密表';

-- 插入菜单
INSERT INTO `sys_menu` VALUES (2000, '脚本管理', 0, 5, 'script', NULL, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2024-01-01 00:00:00', '', NULL, '脚本相关功能菜单');
INSERT INTO `sys_menu` VALUES (2001, '卡密管理', 2000, 1, 'card', 'script/card/index', '', 1, 0, 'C', '0', '0', 'script:card:list', 'component', 'admin', '2024-01-01 00:00:00', '', NULL, '卡密管理菜单');
INSERT INTO `sys_menu` VALUES (2002, '卡密查询', 2001, 1, '', '', '', 1, 0, 'F', '0', '0', 'script:card:query', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '卡密新增', 2001, 2, '', '', '', 1, 0, 'F', '0', '0', 'script:card:add', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '卡密修改', 2001, 3, '', '', '', 1, 0, 'F', '0', '0', 'script:card:edit', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '卡密删除', 2001, 4, '', '', '', 1, 0, 'F', '0', '0', 'script:card:remove', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '卡密导出', 2001, 5, '', '', '', 1, 0, 'F', '0', '0', 'script:card:export', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2007, '卡密生成', 2001, 6, '', '', '', 1, 0, 'F', '0', '0', 'script:card:generate', '#', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 给管理员角色分配权限
INSERT INTO `sys_role_menu` SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2007;