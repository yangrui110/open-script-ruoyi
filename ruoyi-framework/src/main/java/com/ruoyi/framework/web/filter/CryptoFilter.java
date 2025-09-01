package com.ruoyi.framework.web.filter;

import com.ruoyi.common.annotation.ApiEncrypt;
import com.ruoyi.common.config.EncryptProperties;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * API加密过滤器
 * 处理HTTP请求和响应的加密解密
 *
 * @author ruoyi
 */
public class CryptoFilter implements Filter {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CryptoFilter.class);
    
    private final EncryptProperties properties;

    public CryptoFilter(EncryptProperties properties) {
        this.properties = properties;
        log.info("CryptoFilter 初始化成功，headerFlag: {}", properties.getHeaderFlag());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        
        log.debug("CryptoFilter 处理请求: {} {}", servletRequest.getMethod(), servletRequest.getRequestURI());
        
        // 获取加密注解
        ApiEncrypt apiEncrypt = this.getApiEncryptAnnotation(servletRequest);
        boolean responseFlag = apiEncrypt != null && apiEncrypt.response();
        ServletRequest requestWrapper = null;
        ServletResponse responseWrapper = null;
        EncryptResponseBodyWrapper responseBodyWrapper = null;

        // 是否为 put 或者 post 请求
        if (HttpMethod.PUT.matches(servletRequest.getMethod()) || HttpMethod.POST.matches(servletRequest.getMethod())) {
            // 是否存在加密标头
            String headerValue = servletRequest.getHeader(properties.getHeaderFlag());
            if (StringUtils.isNotEmpty(headerValue)) {
                // 请求解密
                requestWrapper = new DecryptRequestBodyWrapper(servletRequest, properties.getPrivateKey(), properties.getHeaderFlag());
            } else {
                // 是否有注解，有就报错，没有放行
                if (apiEncrypt != null) {
                    // 直接返回403错误，不依赖HandlerExceptionResolver
                    servletResponse.setStatus(HttpStatus.FORBIDDEN);
                    servletResponse.setContentType("application/json;charset=UTF-8");
                    servletResponse.getWriter().write("{\"code\":403,\"msg\":\"没有访问权限，请联系管理员授权\"}");
                    return;
                }
            }
        }

        // 判断是否响应加密
        if (responseFlag) {
            responseBodyWrapper = new EncryptResponseBodyWrapper(servletResponse);
            responseWrapper = responseBodyWrapper;
        }

        chain.doFilter(
            requestWrapper != null ? requestWrapper : request,
            responseWrapper != null ? responseWrapper : response);

        if (responseFlag && responseBodyWrapper != null) {
            servletResponse.reset();
            // 对原始内容加密
            String encryptContent = responseBodyWrapper.getEncryptContent(
                servletResponse, properties.getPublicKey(), properties.getHeaderFlag());
            // 对加密后的内容写出
            servletResponse.getWriter().write(encryptContent);
        }
    }

    /**
     * 获取 ApiEncrypt 注解
     */
    private ApiEncrypt getApiEncryptAnnotation(HttpServletRequest servletRequest) {
        try {
            // 使用Bean名称获取HandlerMapping，避免类型冲突
            RequestMappingHandlerMapping handlerMapping = SpringUtils.getBean("requestMappingHandlerMapping");
            // 获取注解
            HandlerExecutionChain mappingHandler = handlerMapping.getHandler(servletRequest);
            if (mappingHandler != null) {
                Object handler = mappingHandler.getHandler();
                if (handler != null && handler instanceof HandlerMethod) {
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    ApiEncrypt apiEncrypt = handlerMethod.getMethodAnnotation(ApiEncrypt.class);
                    if (apiEncrypt != null) {
                        log.debug("找到ApiEncrypt注解，response: {}", apiEncrypt.response());
                    }
                    return apiEncrypt;
                }
            }
        } catch (Exception e) {
            log.debug("获取ApiEncrypt注解时出错: {}", e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public void destroy() {
    }
} 