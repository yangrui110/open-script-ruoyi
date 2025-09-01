package com.ruoyi.common.enums;

/**
 * 加密算法类型枚举
 *
 * @author ruoyi
 */
public enum AlgorithmType {

    /**
     * 默认走配置文件配置
     */
    DEFAULT,

    /**
     * base64编码
     */
    BASE64,

    /**
     * AES加密
     */
    AES,

    /**
     * RSA加密
     */
    RSA,

    /**
     * SM2加密
     */
    SM2,

    /**
     * SM4加密
     */
    SM4;

} 