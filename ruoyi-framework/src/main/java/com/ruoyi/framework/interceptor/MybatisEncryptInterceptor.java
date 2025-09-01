package com.ruoyi.framework.interceptor;

import com.ruoyi.common.annotation.EncryptField;
import com.ruoyi.common.config.EncryptProperties;
import com.ruoyi.common.core.encrypt.EncryptContext;
import com.ruoyi.common.core.encrypt.EncryptorManager;
import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.enums.EncodeType;
import com.ruoyi.common.utils.StringUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
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
import java.sql.PreparedStatement;
import java.util.*;

/**
 * MyBatis加密拦截器
 * 在数据入库前对标注了@EncryptField注解的字段进行加密
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "ruoyi.encrypt.enable", havingValue = "true")
@Intercepts({@Signature(
    type = ParameterHandler.class,
    method = "setParameters",
    args = {PreparedStatement.class})
})
public class MybatisEncryptInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisEncryptInterceptor.class);

    @Autowired
    private EncryptorManager encryptorManager;

    @Autowired
    private EncryptProperties encryptProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterHandler) {
            ParameterHandler parameterHandler = (ParameterHandler) target;
            // 进行加密操作
            Object parameterObject = parameterHandler.getParameterObject();
            if (parameterObject != null && !(parameterObject instanceof String)) {
                this.encryptHandler(parameterObject);
            }
        }
        return target;
    }

    /**
     * 加密对象
     *
     * @param sourceObject 待加密对象
     */
    private void encryptHandler(Object sourceObject) {
        if (sourceObject == null) {
            return;
        }
        if (sourceObject instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) sourceObject;
            new HashSet<>(map.values()).forEach(this::encryptHandler);
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
            list.forEach(this::encryptHandler);
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
                if (fieldValue != null) {
                    field.set(sourceObject, this.encryptField(String.valueOf(fieldValue), field));
                }
            }
        } catch (Exception e) {
            log.error("处理加密字段时出错", e);
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
     * 字段值进行加密
     *
     * @param value 待加密的值
     * @param field 待加密字段
     * @return 加密后结果
     */
    private String encryptField(String value, Field field) {
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
        return this.encryptorManager.encrypt(value, encryptContext);
    }

    @Override
    public void setProperties(Properties properties) {
    }
} 