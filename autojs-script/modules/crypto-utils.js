/**
 * 加解密工具模块
 * 支持RSA和AES加解密，用于与后端API加密通信
 * 基于Java原生实现，兼容AutoJS6环境
 */
function CryptoUtils() {
    
    /**
     * Base64编码
     * @param {string} data 待编码数据
     * @returns {string} Base64编码后的字符串
     */
    function encodeBase64(data) {
        try {
            var bytes = new java.lang.String(data).getBytes("UTF-8");
            var encoder = java.util.Base64.getEncoder();
            return encoder.encodeToString(bytes);
        } catch (e) {
            console.error('[CryptoUtils] Base64编码失败:', e);
            throw new Error('Base64编码失败: ' + e.message);
        }
    }
    
    /**
     * Base64解码
     * @param {string} encodedData Base64编码的数据
     * @returns {string} 解码后的字符串
     */
    function decodeBase64(encodedData) {
        try {
            var decoder = java.util.Base64.getDecoder();
            var bytes = decoder.decode(encodedData);
            return new java.lang.String(bytes, "UTF-8");
        } catch (e) {
            console.error('[CryptoUtils] Base64解码失败:', e);
            throw new Error('Base64解码失败: ' + e.message);
        }
    }
    
    /**
     * 生成随机AES密钥
     * @param {number} length 密钥长度，默认32位
     * @returns {string} 随机AES密钥
     */
    function generateAesKey(length) {
        length = length || 32;
        var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        var result = '';
        var random = new java.security.SecureRandom();
        
        for (var i = 0; i < length; i++) {
            result += chars.charAt(random.nextInt(chars.length));
        }
        return result;
    }
    
    /**
     * AES加密
     * @param {string} data 待加密数据
     * @param {string} key AES密钥
     * @returns {string} Base64编码的加密结果
     */
    function encryptAes(data, key) {
        try {
            console.log('[CryptoUtils] 开始AES加密，数据长度:', data.length, '密钥长度:', key.length);
            
            // 创建AES密钥
            var keyBytes = new java.lang.String(key).getBytes("UTF-8");
            var secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            
            // 创建加密器
            var cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
            
            // 加密数据
            var dataBytes = new java.lang.String(data).getBytes("UTF-8");
            var encryptedBytes = cipher.doFinal(dataBytes);
            
            // Base64编码
            var encoder = java.util.Base64.getEncoder();
            var result = encoder.encodeToString(encryptedBytes);
            
            console.log('[CryptoUtils] AES加密完成，结果长度:', result.length);
            return result;
        } catch (e) {
            console.error('[CryptoUtils] AES加密失败:', e);
            throw new Error('AES加密失败: ' + e.message);
        }
    }
    
    /**
     * AES解密
     * @param {string} encryptedData Base64编码的加密数据
     * @param {string} key AES密钥
     * @returns {string} 解密后的数据
     */
    function decryptAes(encryptedData, key) {
        try {
            console.log('[CryptoUtils] 开始AES解密，数据长度:', encryptedData.length, '密钥长度:', key.length);
            
            // 创建AES密钥
            var keyBytes = new java.lang.String(key).getBytes("UTF-8");
            var secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            
            // 创建解密器
            var cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
            
            // Base64解码
            var decoder = java.util.Base64.getDecoder();
            var encryptedBytes = decoder.decode(encryptedData);
            
            // 解密数据
            var decryptedBytes = cipher.doFinal(encryptedBytes);
            var result = new java.lang.String(decryptedBytes, "UTF-8");
            
            console.log('[CryptoUtils] AES解密完成，结果长度:', result.length);
            return result;
        } catch (e) {
            console.error('[CryptoUtils] AES解密失败:', e);
            throw new Error('AES解密失败: ' + e.message);
        }
    }
    
    /**
     * RSA公钥加密
     * @param {string} data 待加密数据
     * @param {string} publicKeyStr 公钥字符串
     * @returns {string} Base64编码的加密结果
     */
    function encryptRsa(data, publicKeyStr) {
        try {
            console.log('[CryptoUtils] 开始RSA加密，数据长度:', data.length);
            
            // 解码公钥
            var decoder = java.util.Base64.getDecoder();
            var publicKeyBytes = decoder.decode(publicKeyStr);
            var keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            var keyFactory = java.security.KeyFactory.getInstance("RSA");
            var publicKey = keyFactory.generatePublic(keySpec);
            
            // 创建加密器
            var cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            
            // 加密数据
            var dataBytes = new java.lang.String(data).getBytes("UTF-8");
            var encryptedBytes = cipher.doFinal(dataBytes);
            
            // Base64编码
            var encoder = java.util.Base64.getEncoder();
            var result = encoder.encodeToString(encryptedBytes);
            
            console.log('[CryptoUtils] RSA加密完成，结果长度:', result.length);
            return result;
        } catch (e) {
            console.error('[CryptoUtils] RSA加密失败:', e);
            throw new Error('RSA加密失败: ' + e.message);
        }
    }
    
    /**
     * RSA私钥解密
     * @param {string} encryptedData Base64编码的加密数据
     * @param {string} privateKeyStr 私钥字符串
     * @returns {string} 解密后的数据
     */
    function decryptRsa(encryptedData, privateKeyStr) {
        try {
            console.log('[CryptoUtils] 开始RSA解密，数据长度:', encryptedData.length);
            
            // 解码私钥
            var decoder = java.util.Base64.getDecoder();
            var privateKeyBytes = decoder.decode(privateKeyStr);
            var keySpec = new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes);
            var keyFactory = java.security.KeyFactory.getInstance("RSA");
            var privateKey = keyFactory.generatePrivate(keySpec);
            
            // 创建解密器
            var cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
            
            // Base64解码加密数据
            var encryptedBytes = decoder.decode(encryptedData);
            
            // 解密数据
            var decryptedBytes = cipher.doFinal(encryptedBytes);
            var result = new java.lang.String(decryptedBytes, "UTF-8");
            
            console.log('[CryptoUtils] RSA解密完成，结果长度:', result.length);
            return result;
        } catch (e) {
            console.error('[CryptoUtils] RSA解密失败:', e);
            throw new Error('RSA解密失败: ' + e.message);
        }
    }
    
    /**
     * 为请求数据进行加密（模拟前端加密流程）
     * @param {Object|string} data 请求数据
     * @param {string} publicKey RSA公钥
     * @returns {Object} 包含加密后的数据和头部信息
     */
    function encryptRequestData(data, publicKey) {
        try {
            console.log('[CryptoUtils] 开始加密请求数据');
            
            // 1. 生成随机AES密钥
            var aesKey = generateAesKey(32);
            console.log('[CryptoUtils] 生成AES密钥:', aesKey);
            
            // 2. Base64编码AES密钥
            var aesKeyBase64 = encodeBase64(aesKey);
            console.log('[CryptoUtils] AES密钥Base64编码:', aesKeyBase64);
            
            // 3. RSA公钥加密Base64编码的AES密钥
            var encryptedAesKey = encryptRsa(aesKeyBase64, publicKey);
            console.log('[CryptoUtils] RSA加密AES密钥完成');
            
            // 4. 准备请求数据
            var jsonData = typeof data === 'string' ? data : JSON.stringify(data);
            console.log('[CryptoUtils] 请求数据JSON:', jsonData);
            
            // 5. AES加密请求数据
            var encryptedData = encryptAes(jsonData, aesKey);
            console.log('[CryptoUtils] AES加密请求数据完成');
            
            return {
                encryptedData: encryptedData,
                encryptHeader: encryptedAesKey,
                aesKey: aesKey // 用于调试，实际使用时可以移除
            };
        } catch (e) {
            console.error('[CryptoUtils] 加密请求数据失败:', e);
            throw new Error('加密请求数据失败: ' + e.message);
        }
    }
    
    /**
     * 解密响应数据（模拟前端解密流程）
     * @param {string} encryptedData 加密的响应数据
     * @param {string} encryptHeader 响应头中的加密密钥
     * @param {string} privateKey RSA私钥
     * @returns {Object} 解密后的响应数据
     */
    function decryptResponseData(encryptedData, encryptHeader, privateKey) {
        try {
            console.log('[CryptoUtils] 开始解密响应数据');
            
            // 1. RSA私钥解密得到Base64编码的AES密钥
            var aesKeyBase64 = decryptRsa(encryptHeader, privateKey);
            console.log('[CryptoUtils] RSA解密AES密钥完成');
            
            // 2. Base64解码得到AES密钥
            var aesKey = decodeBase64(aesKeyBase64);
            console.log('[CryptoUtils] AES密钥Base64解码完成');
            
            // 3. AES解密响应数据
            var decryptedData = decryptAes(encryptedData, aesKey);
            console.log('[CryptoUtils] AES解密响应数据完成');
            
            // 4. 解析JSON
            var result = JSON.parse(decryptedData);
            console.log('[CryptoUtils] 响应数据解析完成');
            
            return result;
        } catch (e) {
            console.error('[CryptoUtils] 解密响应数据失败:', e);
            throw new Error('解密响应数据失败: ' + e.message);
        }
    }
    
    return {
        encodeBase64: encodeBase64,
        decodeBase64: decodeBase64,
        generateAesKey: generateAesKey,
        encryptAes: encryptAes,
        decryptAes: decryptAes,
        encryptRsa: encryptRsa,
        decryptRsa: decryptRsa,
        encryptRequestData: encryptRequestData,
        decryptResponseData: decryptResponseData
    };
}

// 导出模块实例
module.exports = new CryptoUtils();