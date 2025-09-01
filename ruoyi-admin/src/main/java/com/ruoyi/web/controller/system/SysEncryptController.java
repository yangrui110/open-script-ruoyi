package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.ApiEncrypt;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysUserExample;
import org.springframework.web.bind.annotation.*;

/**
 * 加密功能示例控制器
 * 展示如何使用API加密功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/encrypt")
public class SysEncryptController extends BaseController {

    /**
     * 普通接口（不加密）
     */
    @GetMapping("/normal")
    public AjaxResult normalApi() {
        return AjaxResult.success("这是一个普通接口，不进行加密");
    }

    /**
     * 响应加密接口
     * 使用@ApiEncrypt(response = true)标注，响应数据将被加密
     */
    @ApiEncrypt(response = true)
    @GetMapping("/encrypted-response")
    public AjaxResult encryptedResponse() {
        SysUserExample user = new SysUserExample();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setNickName("测试用户");
        user.setEmail("test@example.com");
        user.setPhonenumber("13800138000");
        user.setIdCard("123456789012345678");
        user.setBankCard("6222000000000000001");
        
        return AjaxResult.success("响应加密测试", user);
    }

    /**
     * 请求解密接口
     * 客户端需要发送加密的请求数据和加密头
     */
    @ApiEncrypt
    @PostMapping("/encrypted-request")
    public AjaxResult encryptedRequest(@RequestBody SysUserExample user) {
        // 这里接收到的数据已经被自动解密
        return AjaxResult.success("请求解密成功", user);
    }

    /**
     * 请求和响应都加密的接口
     */
    @ApiEncrypt(response = true)
    @PostMapping("/full-encrypted")
    public AjaxResult fullEncrypted(@RequestBody SysUserExample user) {
        // 请求数据自动解密，响应数据自动加密
        user.setNickName("处理后的" + user.getNickName());
        return AjaxResult.success("全加密接口处理成功", user);
    }
} 