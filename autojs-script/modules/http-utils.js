/**
 * HTTP工具类
 * 基于Java原生实现，不依赖AutoJS的http模块
 * 统一处理认证头部（clientid 和 JWT token）
 * 支持API加解密功能
 */
function HttpUtils() {
    // 导入必要模块
    var cryptoUtils = require('./crypto-utils.js');
    var config = require('./config.js');
    
    // 默认超时时间（毫秒）
    var DEFAULT_TIMEOUT = 30000;
    
    // 默认User-Agent
    var DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    // 认证配置
    var authConfig = {
        clientId: "f36c69cd4655566bbfac652e479cb931", // 默认客户端ID
        tokenStorageKey: "cardToken", // token存储键名
        authStorageName: "auth" // 存储空间名称
    };
    
    /**
     * 获取加密配置
     * @returns {Object} 加密配置对象
     */
    function getEncryptionConfig() {
        return config.API_CONFIG.ENCRYPTION;
    }
    
    /**
     * 设置认证配置
     * @param {Object} config 认证配置对象
     */
    function setAuthConfig(config) {
        if (config.clientId) {
            authConfig.clientId = config.clientId;
        }
        if (config.tokenStorageKey) {
            authConfig.tokenStorageKey = config.tokenStorageKey;
        }
        if (config.authStorageName) {
            authConfig.authStorageName = config.authStorageName;
        }
    }
    

    
    /**
     * 获取认证头部
     * @param {boolean} skipAuth 是否跳过认证（用于登录等不需要token的接口）
     * @returns {Object} 认证头部对象
     */
    function getAuthHeaders(skipAuth) {
        var headers = {};
        
        // 添加客户端ID
        if (authConfig.clientId) {
            headers['clientid'] = authConfig.clientId;
        }
        
        // 添加JWT token（除非明确跳过认证）
        if (!skipAuth) {
            try {
                var storage = storages.create(authConfig.authStorageName);
                var token = storage.get(authConfig.tokenStorageKey, "");
                if (token) {
                    headers['Authorization'] = 'Bearer ' + token;
                }
            } catch (e) {
                console.warn('[HttpUtils] 获取认证token失败:', e.message);
            }
        }
        
        return headers;
    }
    
    /**
     * 合并请求头部
     * @param {Object} customHeaders 自定义头部
     * @param {boolean} skipAuth 是否跳过认证
     * @returns {Object} 合并后的头部
     */
    function mergeHeaders(customHeaders, skipAuth) {
        var authHeaders = getAuthHeaders(skipAuth);
        var mergedHeaders = Object.assign({}, authHeaders, customHeaders || {});
        
        // 默认Content-Type
        if (!mergedHeaders['Content-Type']) {
            mergedHeaders['Content-Type'] = 'application/json';
        }
        
        return mergedHeaders;
    }
    
    /**
     * 发送GET请求
     * @param {string} url 请求URL
     * @param {Object} options 请求选项，可以包含 responseType: 'text'|'binary', skipAuth: boolean
     * @returns {Object} 响应对象
     */
    function get(url, options) {
        options = options || {};
        return request('GET', url, null, options);
    }
    
    /**
     * 发送POST请求
     * @param {string} url 请求URL
     * @param {Object|string} data 请求数据
     * @param {Object} options 请求选项，可以包含 responseType: 'text'|'binary', skipAuth: boolean
     * @returns {Object} 响应对象
     */
    function post(url, data, options) {
        options = options || {};
        return request('POST', url, data, options);
    }
    
    /**
     * 发送不带认证的POST请求（用于登录等场景）
     * @param {string} url 请求URL
     * @param {Object|string} data 请求数据
     * @param {Object} options 请求选项，可包含 encrypt: boolean
     * @returns {Object} 响应对象
     */
    function postWithoutAuth(url, data, options) {
        options = options || {};
        options.skipAuth = true;
        return post(url, data, options);
    }
    
    /**
     * 发送HTTP请求
     * @param {string} method 请求方法
     * @param {string} url 请求URL
     * @param {Object|string} data 请求数据
     * @param {Object} options 请求选项，可包含 encrypt: boolean 强制加密选项
     * @returns {Object} 响应对象
     */
    function request(method, url, data, options) {
        options = options || {};
        var result = null;
        var error = null;
        
        // 检查是否需要加密 - 直接从配置文件读取
        var encryptionConfig = getEncryptionConfig();
        var needEncrypt = encryptionConfig && encryptionConfig.ENABLED && (method === 'POST' || method === 'PUT') && data;
        console.log('[HttpUtils] 加密检查:', {
            configExists: !!encryptionConfig,
            globalEnabled: encryptionConfig ? encryptionConfig.ENABLED : false,
            method: method,
            hasData: !!data,
            needEncrypt: needEncrypt
        });
        // 处理请求数据加密
        var encryptedData = null;
        var originalData = data;
        
        if (needEncrypt) {
            try {
                console.log('[HttpUtils] 开始加密请求数据...');
                var encryptResult = cryptoUtils.encryptRequestData(data, encryptionConfig.PUBLIC_KEY);
                encryptedData = encryptResult.encryptedData;
                data = encryptedData; // 替换为加密后的数据
                
                // 添加加密头部
                options.headers = options.headers || {};
                options.headers[encryptionConfig.HEADER_FLAG] = encryptResult.encryptHeader;
                
                console.log('[HttpUtils] 请求数据加密完成，加密头部已设置');
            } catch (e) {
                console.error('[HttpUtils] 请求数据加密失败:', e);
                throw new Error('请求数据加密失败: ' + e.message);
            }
        }
        
        // 合并认证头部和自定义头部
        var finalHeaders = mergeHeaders(options.headers, options.skipAuth);
        
        // 打印请求入参
        console.log('====== HTTP请求开始 ======');
        console.log('请求方法:', method);
        console.log('请求URL:', url);
        console.log('是否加密:', needEncrypt);
        if (needEncrypt) {
            console.log('原始数据:', originalData ? (typeof originalData === 'object' ? JSON.stringify(originalData, null, 2) : originalData) : '无');
            console.log('加密数据长度:', encryptedData ? encryptedData.length : 0);
        } else {
            console.log('请求数据:', data ? (typeof data === 'object' ? JSON.stringify(data, null, 2) : data) : '无');
        }
        console.log('跳过认证:', !!options.skipAuth);
        console.log('最终请求头:', JSON.stringify(finalHeaders, null, 2));
        console.log('==========================');
        
        // 创建线程执行网络请求
        var thread = new java.lang.Thread(new java.lang.Runnable({
            run: function() {
                try {
                    console.log('开始执行HTTP请求:', method, url);
                    // 创建URL对象
                    var urlObj = new java.net.URL(url);
                    var connection = urlObj.openConnection();
                    
                    // 设置请求方法
                    connection.setRequestMethod(method);
                    
                    // 设置超时
                    connection.setConnectTimeout(options.timeout || DEFAULT_TIMEOUT);
                    connection.setReadTimeout(options.timeout || DEFAULT_TIMEOUT);
                    
                    // 设置默认User-Agent
                    console.log('设置默认User-Agent:', options.userAgent || DEFAULT_USER_AGENT);
                    connection.setRequestProperty('User-Agent', options.userAgent || DEFAULT_USER_AGENT);
                    
                    // 设置所有请求头（包括认证头部）
                    console.log('设置请求头:', JSON.stringify(finalHeaders, null, 2));
                    for (var key in finalHeaders) {
                        console.log('  -> 设置请求头:', key, '=', finalHeaders[key]);
                        connection.setRequestProperty(key, finalHeaders[key]);
                    }
                    
                    // 验证请求头是否设置成功
                    try {
                        var requestProperties = connection.getRequestProperties();
                        if (requestProperties) {
                            console.log('已设置的所有请求头:');
                            var names = requestProperties.keySet().toArray();
                            for (var i = 0; i < names.length; i++) {
                                var name = names[i];
                                var value = requestProperties.get(name);
                                console.log('  - ', name, ':', value);
                            }
                        }
                    } catch (e) {
                        console.error('无法获取已设置的请求头:', e);
                    }
                    
                    // 处理POST数据
                    if (method === 'POST' && data) {
                        console.log('准备发送POST数据...');
                        connection.setDoOutput(true);
                        var outputStream = connection.getOutputStream();
                        var writer = new java.io.OutputStreamWriter(outputStream, 'UTF-8');
                        
                        var postData;
                        if (typeof data === 'string') {
                            postData = data;
                            console.log('POST数据(字符串):', postData);
                        } else {
                            postData = JSON.stringify(data);
                            console.log('POST数据(JSON):', JSON.stringify(data, null, 2));
                        }
                        console.log('POST数据长度:', postData.length, '字节');
                        
                        writer.write(postData);
                        writer.flush();
                        writer.close();
                        console.log('POST数据发送完成');
                    } else if (method === 'POST') {
                        console.log('POST请求无数据体');
                    }
                    
                    // 获取响应
                    var responseCode = connection.getResponseCode();
                    var responseMessage = connection.getResponseMessage();
                    console.log('收到HTTP响应 - 状态码:', responseCode, '状态消息:', responseMessage);
                    
                    // 读取响应内容
                    var responseBody;
                    try {
                        var inputStream = connection.getInputStream();
                        
                        // 根据responseType确定如何处理响应体
                        if (options.responseType === 'binary') {
                            responseBody = readInputStreamAsBinary(inputStream);
                        } else {
                            responseBody = readInputStreamAsText(inputStream, connection);
                        }
                    } catch (e) {
                        console.error('读取响应内容失败:', e);
                        var errorStream = connection.getErrorStream();
                        if (errorStream) {
                            if (options.responseType === 'binary') {
                                responseBody = readInputStreamAsBinary(errorStream);
                            } else {
                                responseBody = readInputStreamAsText(errorStream, connection);
                            }
                        }
                    }
                    
                    // 获取响应头
                    var headers = {};
                    var headerFields = connection.getHeaderFields();
                    var headerIterator = headerFields.keySet().iterator();
                    console.log('解析响应头...');
                    while (headerIterator.hasNext()) {
                        var headerKey = headerIterator.next();
                        if (headerKey) {
                            headers[headerKey] = headerFields.get(headerKey).get(0);
                            console.log('  响应头:', headerKey, '=', headers[headerKey]);
                        }
                    }
                    
                    // 构建响应对象
                    result = {
                        statusCode: responseCode,
                        statusMessage: responseMessage,
                        headers: headers,
                        body: responseBody,
                        // 添加额外属性，便于处理二进制数据
                        isBinary: options.responseType === 'binary',
                        contentLength: connection.getContentLength(),
                        contentType: connection.getContentType()
                    };
                    
                    // 处理响应解密
                    if (needEncrypt && result.statusCode === 200 && !result.isBinary) {
                        try {
                            // 检查响应头中是否有加密标识
                            var responseEncryptionConfig = getEncryptionConfig();
                            var encryptHeader = headers[responseEncryptionConfig.HEADER_FLAG];
                            if (encryptHeader && responseEncryptionConfig && responseEncryptionConfig.PRIVATE_KEY) {
                                console.log('[HttpUtils] 开始解密响应数据...');
                                var decryptedBody = cryptoUtils.decryptResponseData(
                                    result.body, 
                                    encryptHeader, 
                                    responseEncryptionConfig.PRIVATE_KEY
                                );
                                // 将解密后的对象转回JSON字符串，保持与原有逻辑一致
                                result.body = JSON.stringify(decryptedBody);
                                console.log('[HttpUtils] 响应数据解密完成');
                            }
                        } catch (e) {
                            console.error('[HttpUtils] 响应数据解密失败:', e);
                            // 解密失败不抛出异常，使用原始响应体
                            console.warn('[HttpUtils] 使用原始响应体继续处理');
                        }
                    }
                    
                    // 打印响应结果
                    console.log('====== HTTP响应结果 ======');
                    console.log('状态码:', result.statusCode);
                    console.log('状态消息:', result.statusMessage);
                    console.log('内容类型:', result.contentType);
                    console.log('内容长度:', result.contentLength);
                    console.log('是否二进制:', result.isBinary);
                    
                    // 打印响应体（根据类型决定如何显示）
                    if (result.isBinary) {
                        console.log('响应体(二进制):', result.body ? result.body.length + ' 字节' : '无');
                    } else {
                        var bodyStr = result.body ? result.body.toString() : '';
                        console.log('响应体长度:', bodyStr.length, '字符');
                        if (bodyStr.length > 0) {
                            // 尝试格式化JSON
                            try {
                                var jsonData = JSON.parse(bodyStr);
                                console.log('响应体(JSON):', JSON.stringify(jsonData, null, 2));
                            } catch (e) {
                                // 不是JSON，直接输出
                                if (bodyStr.length > 500) {
                                    console.log('响应体(文本，前500字符):', bodyStr.substring(0, 500) + '...');
                                } else {
                                    console.log('响应体(文本):', bodyStr);
                                }
                            }
                        } else {
                            console.log('响应体: 空');
                        }
                    }
                    console.log('==========================');
                    
                } catch (e) {
                    console.error('HTTP请求失败:', e);
                    error = new Error('HTTP请求失败: ' + e.message);
                }
            }
        }));
        
        // 启动线程
        thread.start();
        
        // 等待线程完成
        try {
            thread.join();
        } catch (e) {
            console.error('等待线程完成失败:', e);
            throw new Error('等待线程完成失败: ' + e.message);
        }
        
        // 检查是否有错误
        if (error) {
            console.log('====== HTTP请求失败 ======');
            console.log('错误信息:', error.message);
            console.log('==========================');
            throw error;
        }
        
        console.log('====== HTTP请求完成 ======');
        console.log('请求成功，状态码:', result ? result.statusCode : 'unknown');
        console.log('==========================');
        
        return result;
    }
    
    /**
     * 以文本方式读取输入流
     * @param {java.io.InputStream} inputStream 输入流
     * @param {java.net.URLConnection} connection URL连接对象
     * @returns {string} 读取的文本内容
     */
    function readInputStreamAsText(inputStream, connection) {
        try {
            console.log('以文本方式读取输入流');
            var ByteArrayOutputStream = java.io.ByteArrayOutputStream;
            var buffer = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 4096);
            var baos = new ByteArrayOutputStream();
            var len;
            var totalBytes = 0;
            
            while ((len = inputStream.read(buffer)) !== -1) {
                baos.write(buffer, 0, len);
                totalBytes += len;
            }
            
            var bytes = baos.toByteArray();
            
            baos.close();
            inputStream.close();
            
            // 尝试检测编码
            var charset = 'UTF-8';
            var contentType = connection.getContentType();
            
            if (contentType && contentType.indexOf('charset=') !== -1) {
                charset = contentType.split('charset=')[1].trim();
            }
            
            var result = new java.lang.String(bytes, charset);
            console.log('读取完成，文本长度:', result.length());
            return result;
        } catch (e) {
            console.error('读取响应内容失败:', e);
            return '';
        }
    }
    
    /**
     * 以二进制方式读取输入流
     * @param {java.io.InputStream} inputStream 输入流
     * @returns {java.nio.ByteBuffer} 读取的二进制内容
     */
    function readInputStreamAsBinary(inputStream) {
        try {
            console.log('以二进制方式读取输入流');
            var ByteArrayOutputStream = java.io.ByteArrayOutputStream;
            var buffer = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 8192);
            var baos = new ByteArrayOutputStream();
            var len;
            var totalBytes = 0;
            
            while ((len = inputStream.read(buffer)) !== -1) {
                baos.write(buffer, 0, len);
                totalBytes += len;
                
                // 每读取10MB显示一次进度
                if (totalBytes % (10 * 1024 * 1024) < 8192) {
                    console.log('已读取:', (totalBytes / 1024 / 1024).toFixed(2) + 'MB');
                }
            }
            
            var bytes = baos.toByteArray();
            console.log('读取完成，二进制数据大小:', bytes.length + ' 字节');
            
            baos.close();
            inputStream.close();
            
            // 返回Java字节数组
            return bytes;
        } catch (e) {
            console.error('读取二进制内容失败:', e);
            return null;
        }
    }
    
    /**
     * 下载文件
     * @param {string} url 文件URL
     * @param {string} savePath 保存路径
     * @param {Object} options 请求选项
     * @returns {string} 保存的文件路径
     */
    function download(filePath, url, options) {
        options = options || {};
        
        // 指定responseType为binary
        options.responseType = 'binary';
        
        // 添加用于下载的请求头
        options.headers = options.headers || {};
        if (!options.headers['Accept']) {
            options.headers['Accept'] = '*/*';
        }
        if (!options.headers['Accept-Encoding']) {
            options.headers['Accept-Encoding'] = 'identity'; // 不使用压缩
        }
        
        console.log('====== 开始文件下载 ======');
        console.log('下载URL:', url);
        console.log('保存路径:', filePath);
        console.log('下载选项:', JSON.stringify(options, null, 2));
        console.log('==========================');
        
        try {
            // 使用request方法获取二进制数据
            var response = get(url, options);
            
            if (response.statusCode !== 200) {
                throw new Error('下载失败, HTTP状态码: ' + response.statusCode);
            }
            
            // 确保目录存在
            var file = new java.io.File(filePath);
            var parentDir = file.getParentFile();
            if (parentDir && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 将二进制数据写入文件
            var fos = new java.io.FileOutputStream(file);
            fos.write(response.body);
            fos.flush();
            fos.close();
            
            console.log('====== 文件下载完成 ======');
            console.log('文件大小:', response.body.length + ' 字节');
            console.log('保存位置:', filePath);
            console.log('==========================');
            return filePath;
            
        } catch (e) {
            console.log('====== 文件下载失败 ======');
            console.error('错误信息:', e);
            console.log('==========================');
            throw e;
        }
    }
    
    /**
     * 保存二进制响应体到文件
     * @param {Object} response HTTP响应对象
     * @param {string} filePath 文件保存路径
     * @returns {string} 保存的文件路径
     */
    function saveResponseToFile(response, filePath) {
        if (!response || !response.body) {
            throw new Error('无效的响应对象');
        }
        
        try {
            // 确保目录存在
            var file = new java.io.File(filePath);
            var parentDir = file.getParentFile();
            if (parentDir && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 写入文件
            if (response.isBinary) {
                // 二进制响应
                var fos = new java.io.FileOutputStream(file);
                fos.write(response.body);
                fos.close();
            } else {
                // 文本响应
                files.write(filePath, response.body.toString());
            }
            
            return filePath;
        } catch (e) {
            console.error('保存文件失败:', e);
            throw e;
        }
    }
    

    
    return {
        get: get,
        post: post,
        postWithoutAuth: postWithoutAuth,
        request: request,
        download: download,
        saveResponseToFile: saveResponseToFile,
        setAuthConfig: setAuthConfig,
        getAuthHeaders: getAuthHeaders
    };
}

// 导出模块实例
module.exports = new HttpUtils(); 