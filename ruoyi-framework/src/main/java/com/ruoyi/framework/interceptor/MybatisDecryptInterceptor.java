package com.ruoyi.framework.interceptor;

import com.ruoyi.common.annotation.EncryptField;
import com.ruoyi.common.config.EncryptProperties;
import com.ruoyi.common.core.encrypt.EncryptContext;
import com.ruoyi.common.core.encrypt.EncryptorManager;
import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;
import com.ruoyi.common.utils.StringUtils;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.*;

/**
 * MyBatis解密拦截器
 * 在数据查询后对标注了@EncryptField注解的字段进行解密
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "ruoyi.encrypt.enable", havingValue = "true")
@Intercepts({@Signature(
    type = ResultSetHandler.class,
    method = "handleResultSets",
    args = {Statement.class})
})
public class MybatisDecryptInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisDecryptInterceptor.class);

    @Autowired
    private EncryptorManager encryptorManager;

    @Autowired
    private EncryptProperties encryptProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result != null) {
            this.decryptHandler(result);
        }
        return result;
    }

    /**
     * 解密对象
     *
     * @param sourceObject 待解密对象
     */
    private void decryptHandler(Object sourceObject) {
        if (sourceObject == null) {
            return;
        }
        if (sourceObject instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) sourceObject;
            new HashSet<>(map.values()).forEach(this::decryptHandler);
            return;
        }
        if (sourceObject instanceof List) {
            List<?> list = (List<?>) sourceObject;
            if (list.isEmpty()) {
                return;
            }
            // 判断第一个元素是否含有注解。如果没有直接返回，提高效率
            Object firstItem = list.get(0);
            if (firstItem == null || getEncryptFields(firstItem.getClass()).isEmpty()) {
                return;
            }
            list.forEach(this::decryptHandler);
            return;
        }
        // 处理普通对象
        Set<Field> fields = getEncryptFields(sourceObject.getClass());
        if (fields == null || fields.isEmpty()) {
            return;
        }
        try {
            for (Field field : fields) {
                Object fieldValue = field.get(sourceObject);
                if (fieldValue != null && fieldValue instanceof String) {
                    field.set(sourceObject, this.decryptField((String) fieldValue, field));
                }
            }
        } catch (Exception e) {
            log.error("处理解密字段时出错", e);
        }
    }

    /**
     * 获取类的加密字段
     */
    private Set<Field> getEncryptFields(Class<?> clazz) {
        Set<Field> encryptFields = new HashSet<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(EncryptField.class) && field.getType() == String.class) {
                field.setAccessible(true);
                encryptFields.add(field);
            }
        }
        return encryptFields;
    }

    /**
     * 字段值进行解密
     *
     * @param value 待解密的值
     * @param field 待解密字段
     * @return 解密后结果
     */
    private String decryptField(String value, Field field) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        EncryptField encryptField = field.getAnnotation(EncryptField.class);
        EncryptContext encryptContext = new EncryptContext();
        encryptContext.setAlgorithm(encryptField.algorithm() == AlgorithmType.DEFAULT ? 
            encryptProperties.getAlgorithm() : encryptField.algorithm());
        encryptContext.setEncode(encryptField.encode() == EncodeType.DEFAULT ? 
            encryptProperties.getEncode() : encryptField.encode());
        encryptContext.setPassword(StringUtils.isBlank(encryptField.password()) ? 
            encryptProperties.getPassword() : encryptField.password());
        encryptContext.setPrivateKey(StringUtils.isBlank(encryptField.privateKey()) ? 
            encryptProperties.getPrivateKey() : encryptField.privateKey());
        encryptContext.setPublicKey(StringUtils.isBlank(encryptField.publicKey()) ? 
            encryptProperties.getPublicKey() : encryptField.publicKey());
        return this.encryptorManager.decrypt(value, encryptContext);
    }

    @Override
    public void setProperties(Properties properties) {
    }
} 