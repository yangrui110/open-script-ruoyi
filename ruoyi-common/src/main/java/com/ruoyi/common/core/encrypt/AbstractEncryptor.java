package com.ruoyi.common.core.encrypt;

/**
 * 所有加密执行者的基类
 *
 * @author ruoyi
 */
public abstract class AbstractEncryptor implements IEncryptor {

    protected final EncryptContext context;

    public AbstractEncryptor(EncryptContext context) {
        this.context = context;
    }
} 