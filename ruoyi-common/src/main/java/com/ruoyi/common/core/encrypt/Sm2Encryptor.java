package com.ruoyi.common.core.encrypt;

import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;
import com.ruoyi.common.utils.EncryptUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * SM2算法实现
 *
 * @author ruoyi
 */
public class Sm2Encryptor extends AbstractEncryptor {

    public Sm2Encryptor(EncryptContext context) {
        super(context);
        String privateKey = context.getPrivateKey();
        String publicKey = context.getPublicKey();
        if (StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(publicKey)) {
            throw new IllegalArgumentException("SM2公私钥均需要提供，公钥加密，私钥解密。");
        }
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM2;
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
            return EncryptUtils.encryptBySm2Hex(value, context.getPublicKey());
        } else {
            return EncryptUtils.encryptBySm2(value, context.getPublicKey());
        }
    }

    /**
     * 解密
     *
     * @param value 待解密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptBySm2(value, context.getPrivateKey());
    }
} 