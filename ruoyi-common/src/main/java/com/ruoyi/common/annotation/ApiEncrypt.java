package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * API加密注解
 * 用于标记需要进行请求/响应加密的API接口
 *
 * @author ruoyi
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {

    /**
     * 响应加密开关，默认不加密，为 true 时加密
     */
    boolean response() default false;

} 