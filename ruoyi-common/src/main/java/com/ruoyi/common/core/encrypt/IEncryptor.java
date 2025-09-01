package com.ruoyi.common.core.encrypt;

import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;

/**
 * 加密器接口
 *
 * @author ruoyi
 */
public interface IEncryptor {

    /**
     * 获得当前算法
     */
    AlgorithmType algorithm();

    /**
     * 加密
     *
     * @param value      待加密字符串
     * @param encodeType 加密后的编码格式
     * @return 加密后的字符串
     */
    String encrypt(String value, EncodeType encodeType);

    /**
     * 解密
     *
     * @param value 待解密字符串
     * @return 解密后的字符串
     */
    String decrypt(String value);
} 