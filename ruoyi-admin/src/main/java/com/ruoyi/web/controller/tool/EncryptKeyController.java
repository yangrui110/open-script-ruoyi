package com.ruoyi.web.controller.tool;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.EncryptUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 加密密钥生成工具
 * 用于生成RSA和SM2密钥对
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/tool/encrypt")
public class EncryptKeyController extends BaseController {

    /**
     * 生成RSA密钥对
     */
    @GetMapping("/generateRsaKey")
    public AjaxResult generateRsaKey() {
        Map<String, String> keyMap = EncryptUtils.generateRsaKey();
        return AjaxResult.success("RSA密钥对生成成功", keyMap);
    }

    /**
     * 生成SM2密钥对
     */
    @GetMapping("/generateSm2Key")
    public AjaxResult generateSm2Key() {
        Map<String, String> keyMap = EncryptUtils.generateSm2Key();
        return AjaxResult.success("SM2密钥对生成成功", keyMap);
    }

    /**
     * AES加密测试
     */
    @GetMapping("/testAes")
    public AjaxResult testAes() {
        String originalText = "这是一段需要加密的文本";
        String password = "1234567890123456"; // 16位密钥
        
        String encrypted = EncryptUtils.encryptByAes(originalText, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);
        
        return AjaxResult.success("AES加密测试成功")
                .put("original", originalText)
                .put("encrypted", encrypted)
                .put("decrypted", decrypted);
    }
} 