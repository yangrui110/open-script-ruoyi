package com.ruoyi.common.core.encrypt;

import com.ruoyi.common.annotation.EncryptField;
import com.ruoyi.common.enums.AlgorithmType;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 加密管理器
 *
 * @author ruoyi
 */
public class EncryptorManager {

    private static final Logger log = LoggerFactory.getLogger(EncryptorManager.class);

    /**
     * 缓存加密器
     */
    private final Map<Integer, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 类加密字段缓存
     */
    private final Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 构造方法传入类加密字段缓存
     *
     * @param typeAliasesPackage 实体类包
     */
    public EncryptorManager(String typeAliasesPackage) {
        if (StringUtils.isNotEmpty(typeAliasesPackage)) {
            scanEncryptClasses(typeAliasesPackage);
        }
    }

    /**
     * 获取类加密字段缓存
     */
    public Set<Field> getFieldCache(Class<?> sourceClazz) {
        return fieldCache.get(sourceClazz);
    }

    /**
     * 注册加密执行者到缓存
     *
     * @param encryptContext 加密执行者需要的相关配置参数
     */
    public IEncryptor registAndGetEncryptor(EncryptContext encryptContext) {
        int key = encryptContext.hashCode();
        if (encryptorMap.containsKey(key)) {
            return encryptorMap.get(key);
        }
        IEncryptor encryptor = createEncryptor(encryptContext);
        encryptorMap.put(key, encryptor);
        return encryptor;
    }

    /**
     * 创建加密器实例
     */
    private IEncryptor createEncryptor(EncryptContext context) {
        AlgorithmType algorithm = context.getAlgorithm();
        switch (algorithm) {
            case AES:
                return new AesEncryptor(context);
            case RSA:
                return new RsaEncryptor(context);
            case BASE64:
                return new Base64Encryptor(context);
            case SM2:
                return new Sm2Encryptor(context);
            case SM4:
                return new Sm4Encryptor(context);
            default:
                throw new IllegalArgumentException("不支持的加密算法: " + algorithm);
        }
    }

    /**
     * 根据配置进行加密
     *
     * @param value          待加密的值
     * @param encryptContext 加密相关的配置信息
     */
    public String encrypt(String value, EncryptContext encryptContext) {
        IEncryptor encryptor = this.registAndGetEncryptor(encryptContext);
        return encryptor.encrypt(value, encryptContext.getEncode());
    }

    /**
     * 根据配置进行解密
     *
     * @param value          待解密的值
     * @param encryptContext 加密相关的配置信息
     */
    public String decrypt(String value, EncryptContext encryptContext) {
        IEncryptor encryptor = this.registAndGetEncryptor(encryptContext);
        return encryptor.decrypt(value);
    }

    /**
     * 通过 typeAliasesPackage 设置的扫描包 扫描缓存实体
     */
    private void scanEncryptClasses(String typeAliasesPackage) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
        String[] packagePatternArray = typeAliasesPackage.split(",");
        String classpath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
        try {
            for (String packagePattern : packagePatternArray) {
                String path = ClassUtils.convertClassNameToResourcePath(packagePattern.trim());
                Resource[] resources = resolver.getResources(classpath + path + "/**/*.class");
                for (Resource resource : resources) {
                    ClassMetadata classMetadata = factory.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Class.forName(classMetadata.getClassName());
                    Set<Field> encryptFieldSet = getEncryptFieldSetFromClazz(clazz);
                    if (!encryptFieldSet.isEmpty()) {
                        fieldCache.put(clazz, encryptFieldSet);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化数据安全缓存时出错:{}", e.getMessage());
        }
    }

    /**
     * 获得一个类的加密字段集合
     */
    private Set<Field> getEncryptFieldSetFromClazz(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();
        // 判断clazz如果是接口,内部类,匿名类就直接返回
        if (clazz.isInterface() || clazz.isMemberClass() || clazz.isAnonymousClass()) {
            return fieldSet;
        }
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            fieldSet.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }
        fieldSet = fieldSet.stream().filter(field ->
                field.isAnnotationPresent(EncryptField.class) && field.getType() == String.class)
            .collect(Collectors.toSet());
        for (Field field : fieldSet) {
            field.setAccessible(true);
        }
        return fieldSet;
    }
} 