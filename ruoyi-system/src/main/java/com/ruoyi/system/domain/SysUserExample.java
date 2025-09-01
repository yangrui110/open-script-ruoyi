package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.EncryptField;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户加密示例实体类
 * 展示如何使用@EncryptField注解对敏感字段进行加密
 *
 * @author ruoyi
 */
public class SysUserExample extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户邮箱 - 使用AES加密 */
    @EncryptField
    private String email;

    /** 手机号码 - 使用AES加密 */
    @EncryptField
    private String phonenumber;

    /** 身份证号 - 使用AES加密 */
    @EncryptField
    private String idCard;

    /** 银行卡号 - 使用RSA加密（需要配置公私钥） */
    @EncryptField(algorithm = com.ruoyi.common.enums.AlgorithmType.RSA)
    private String bankCard;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }
} 