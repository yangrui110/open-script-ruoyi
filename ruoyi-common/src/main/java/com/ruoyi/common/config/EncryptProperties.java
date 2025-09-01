package com.ruoyi.common.config;

import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 加密配置属性类
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "ruoyi.encrypt")
public class EncryptProperties {

    /**
     * 数据库字段加密开关
     */
    private Boolean enable = false;

    /**
     * API请求响应加密开关
     */
    private Boolean apiEnable = false;

    /**
     * 默认算法
     */
    private AlgorithmType algorithm = AlgorithmType.AES;

    /**
     * 安全秘钥（AES/SM4使用）
     */
    private String password;

    /**
     * 公钥（RSA/SM2使用）
     */
    private String publicKey;

    /**
     * 私钥（RSA/SM2使用）
     */
    private String privateKey;

    /**
     * 编码方式，base64/hex
     */
    private EncodeType encode = EncodeType.BASE64;

    /**
     * API加密请求头标识
     */
    private String headerFlag = "encrypt-key";

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getApiEnable() {
        return apiEnable;
    }

    public void setApiEnable(Boolean apiEnable) {
        this.apiEnable = apiEnable;
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmType algorithm) {
        this.algorithm = algorithm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public EncodeType getEncode() {
        return encode;
    }

    public void setEncode(EncodeType encode) {
        this.encode = encode;
    }

    public String getHeaderFlag() {
        return headerFlag;
    }

    public void setHeaderFlag(String headerFlag) {
        this.headerFlag = headerFlag;
    }
} 