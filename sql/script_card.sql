-- `ry-vue`.script_card definition

CREATE TABLE `script_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `card_no` varchar(20) NOT NULL COMMENT '卡密',
  `expire_day` int(5) NOT NULL COMMENT '过期天数',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `expire_time` varchar(30) DEFAULT NULL COMMENT '实际过期时间（初始为NULL，第一次绑定时赋值）',
  `device_size` varchar(50) DEFAULT '1' COMMENT '可绑定设备数',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='卡密表';


-- `ry-vue`.script_card_device definition

CREATE TABLE `script_card_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `card_no` varchar(20) NOT NULL COMMENT '卡密',
  `device_android_id` varchar(30) NOT NULL COMMENT '设备的Android ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='卡密关联设备表';


-- `ry-vue`.script_card_game definition

CREATE TABLE `script_card_game` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `card_no` varchar(20) NOT NULL COMMENT '卡号',
  `game_id` bigint(20) NOT NULL COMMENT '游戏ID',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='卡号关联游戏';


-- `ry-vue`.script_device definition

CREATE TABLE `script_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `device_android_id` varchar(30) NOT NULL COMMENT '设备的Android ID',
  `device_width` int(5) DEFAULT '0' COMMENT '宽',
  `device_height` int(5) DEFAULT '0' COMMENT '高',
  `device_build_id` varchar(20) DEFAULT NULL COMMENT '修订版本号',
  `device_broad` varchar(20) DEFAULT '' COMMENT '主板型号',
  `device_brand` varchar(30) DEFAULT '0' COMMENT '厂商品牌',
  `device_name` varchar(30) DEFAULT '0' COMMENT '设备在工业设计中的名称',
  `device_model` varchar(30) DEFAULT '' COMMENT '设备型号',
  `device_sdk_int` varchar(10) DEFAULT NULL COMMENT '安卓系统API版本',
  `device_IMEI` varchar(30) DEFAULT NULL COMMENT '设备IMEI',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='设备表';


-- `ry-vue`.script_game definition

CREATE TABLE `script_game` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(20) NOT NULL COMMENT '游戏名称',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='游戏列表';


-- `ry-vue`.script_log definition

CREATE TABLE `script_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `android_id` varchar(40) NOT NULL COMMENT 'androidId',
  `level` varchar(20) NOT NULL COMMENT 'INFO、WARN、ERROR',
  `tag` varchar(200) NOT NULL COMMENT '标的类型',
  `message` varchar(200) NOT NULL COMMENT '具体信息',
  `extra` text COMMENT '额外的信息',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8 COMMENT='脚本日志表';


-- `ry-vue`.script_version_control definition

CREATE TABLE `script_version_control` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `game_id` bigint(20) NOT NULL COMMENT '游戏ID',
  `file_url` varchar(200) NOT NULL COMMENT '文件地址',
  `type` tinyint(20) NOT NULL COMMENT '文件类型(0:单js文件,1:zip文件)',
  `version` int(5) NOT NULL COMMENT '版本(每次上传。代码版本自动+1)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_dept` bigint(20) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='脚本版本表';