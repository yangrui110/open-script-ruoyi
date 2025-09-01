package com.ruoyi.framework.config;

import com.ruoyi.common.config.EncryptProperties;
import com.ruoyi.common.core.encrypt.EncryptorManager;
import com.ruoyi.framework.web.filter.CryptoFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.DispatcherType;

/**
 * 加密配置类
 *
 * @author ruoyi
 */
@Configuration
public class EncryptConfig {

    @Autowired
    private Environment environment;

    /**
     * 注册加密管理器
     */
    @Bean
    @ConditionalOnProperty(name = "ruoyi.encrypt.enable", havingValue = "true")
    public EncryptorManager encryptorManager() {
        // 从MyBatis配置中获取实体类包路径
        String typeAliasesPackage = environment.getProperty("mybatis.typeAliasesPackage", "com.ruoyi.**.domain");
        return new EncryptorManager(typeAliasesPackage);
    }

    /**
     * 注册API加密过滤器
     */
    @Bean
    @ConditionalOnProperty(name = "ruoyi.encrypt.api-enable", havingValue = "true")
    public FilterRegistrationBean<CryptoFilter> cryptoFilterRegistration(EncryptProperties properties) {
        System.out.println("注册CryptoFilter过滤器，api-enable: " + properties.getApiEnable());
        FilterRegistrationBean<CryptoFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new CryptoFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setName("cryptoFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return registration;
    }
} 