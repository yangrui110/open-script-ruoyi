package com.ruoyi.common.core.encrypt;

import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;
import com.ruoyi.common.utils.EncryptUtils;

/**
 * SM4算法实现
 *
 * @author ruoyi
 */
public class Sm4Encryptor extends AbstractEncryptor {

    public Sm4Encryptor(EncryptContext context) {
        super(context);
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM4;
    }

    /**
     * 加密
     *
     * @param value      待加密字符串
     * @param encodeType 加密后的编码格式
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (encodeType == EncodeType.HEX) {
            return EncryptUtils.encryptBySm4Hex(value, context.getPassword());
        } else {
            return EncryptUtils.encryptBySm4(value, context.getPassword());
        }
    }

    /**
     * 解密
     *
     * @param value 待解密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm4(value, context.getPassword());
    }
} 