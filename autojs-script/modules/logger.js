let httpUtils = require('./http-utils.js');

/**
 * 异步日志管理器
 * 支持本地存储和服务器上传
 * 基于AutoJS6环境开发，支持多线程安全
 */
function Logger() {
    // 日志级别定义
    var LogLevel = {
        DEBUG: 0,
        INFO: 1,
        WARN: 2,
        ERROR: 3
    };
    
    // 日志级别名称映射
    var LogLevelNames = ['DEBUG', 'INFO', 'WARN', 'ERROR'];
    
    // 配置参数
    var config = {
        // 当前日志级别，控制控制台输出和本地文件存储的最低级别
        level: LogLevel.INFO,
        // 服务器上传的最低日志级别，独立控制
        serverLevel: LogLevel.INFO,
        // 本地日志文件路径
        logFilePath: '/sdcard/autojs-logs/',
        // 日志文件最大大小（字节）
        maxFileSize: 5 * 1024 * 1024, // 5MB
        // 最大保留日志文件数量
        maxFiles: 10,
        // 服务器上传配置
        server: {
            url: '', // 服务器地址，需要配置
            batchSize: 50, // 批量上传条数
            uploadInterval: 30000, // 上传间隔（毫秒）
            maxRetries: 3, // 最大重试次数
            timeout: 30000 // 请求超时时间
        },
        // 是否启用控制台输出
        consoleOutput: true,
        // 是否启用本地文件存储
        fileOutput: true,
        // 是否启用服务器上传
        serverUpload: false
    };
    
    // 日志队列（用于异步处理）
    var logQueue = [];
    var uploadQueue = [];
    
    // 线程安全相关
    var queueLock = false;  // 简单的互斥锁
    var fileLock = false;   // 文件写入锁
    
    // 定时器引用
    var processTimer = null;
    var uploadTimer = null;
    
    // 当前日志文件名
    var currentLogFile = null;
    
    // 初始化标志
    var initialized = false;
    
    /**
     * 获取互斥锁（简单实现）
     */
    function acquireLock(lockName, timeout) {
        timeout = timeout || 1000;
        var startTime = Date.now();
        
        while (eval(lockName) && (Date.now() - startTime) < timeout) {
            sleep(1); // 等待1ms
        }
        
        if (eval(lockName)) {
            return false; // 获取锁失败
        }
        
        eval(lockName + ' = true');
        return true; // 获取锁成功
    }
    
    /**
     * 释放互斥锁
     */
    function releaseLock(lockName) {
        eval(lockName + ' = false');
    }
    
    /**
     * 线程安全的队列操作
     */
    function safeQueuePush(item) {
        if (acquireLock('queueLock', 100)) {
            try {
                logQueue.push(item);
                return true;
            } finally {
                releaseLock('queueLock');
            }
        }
        return false;
    }
    
    /**
     * 线程安全的队列取出操作
     */
    function safeQueueSplice(count) {
        if (acquireLock('queueLock', 100)) {
            try {
                if (logQueue.length === 0) {
                    return [];
                }
                return logQueue.splice(0, Math.min(logQueue.length, count));
            } finally {
                releaseLock('queueLock');
            }
        }
        return [];
    }
    
    /**
     * 初始化日志管理器
     * @param {Object} options 配置选项
     */
    function init(options) {
        if (initialized) {
            return;
        }
        
        // 合并配置
        if (options) {
            Object.assign(config, options);
            if (options.server) {
                Object.assign(config.server, options.server);
            }
        }
        
        // 创建日志目录
        try {
            if (config.fileOutput) {
                files.ensureDir(config.logFilePath);
            }
        } catch (e) {
            console.error('[Logger] 创建日志目录失败:', e.message);
        }
        
        // 生成当前日志文件名
        updateCurrentLogFile();
        
        // 启动异步处理
        startProcessing();
        
        // 启动上传服务（如果启用）
        if (config.serverUpload && config.server.url) {
            startUploadService();
        }
        
        initialized = true;
        info('Logger', '日志管理器初始化完成');
    }
    
    /**
     * 更新当前日志文件名
     */
    function updateCurrentLogFile() {
        var now = new Date();
        var dateStr = now.getFullYear() + '-' + 
                     padZero(now.getMonth() + 1) + '-' + 
                     padZero(now.getDate());
        currentLogFile = config.logFilePath + 'autojs-' + dateStr + '.log';
    }
    
    /**
     * 数字补零
     */
    function padZero(num) {
        return num < 10 ? '0' + num : num.toString();
    }
    
    /**
     * 启动异步日志处理
     */
    function startProcessing() {
        if (processTimer) {
            return;
        }
        
        processTimer = setInterval(function() {
            processLogQueue();
        }, 100); // 每100ms处理一次队列
    }
    
    /**
     * 启动上传服务
     */
    function startUploadService() {
        if (uploadTimer) {
            return;
        }
        
        uploadTimer = setInterval(function() {
            uploadLogs();
        }, config.server.uploadInterval);
    }
    
    /**
     * 处理日志队列
     */
    function processLogQueue() {
        if (logQueue.length === 0) {
            return;
        }
        
        var logsToProcess = safeQueueSplice(Math.min(logQueue.length, 10));
        
        for (var i = 0; i < logsToProcess.length; i++) {
            var logEntry = logsToProcess[i];
            
            try {
                // 控制台输出
                if (config.consoleOutput) {
                    outputToConsole(logEntry);
                }
                
                // 文件输出
                if (config.fileOutput) {
                    writeToFile(logEntry);
                }
            } catch (e) {
                console.error('[Logger] 处理日志条目失败:', e.message);
            }
        }
    }
    
    /**
     * 输出到控制台
     */
    function outputToConsole(logEntry) {
        var message = '[' + logEntry.timestamp + '] [' + logEntry.level + '] [' + logEntry.tag + '] ' + logEntry.message;
        
        if (logEntry.extra) {
            message += ' | Extra: ' + JSON.stringify(logEntry.extra);
        }
        
        switch (logEntry.levelValue) {
            case LogLevel.DEBUG:
                console.verbose(message);
                break;
            case LogLevel.INFO:
                console.info(message);
                break;
            case LogLevel.WARN:
                console.warn(message);
                break;
            case LogLevel.ERROR:
                console.error(message);
                break;
            default:
                console.log(message);
        }
    }
    
    /**
     * 写入到文件
     */
    function writeToFile(logEntry) {
        if (!acquireLock('fileLock', 100)) {
            console.warn('[Logger] 文件写入锁获取失败，跳过写入');
            return;
        }
        try {
            // 检查文件大小，如果超过限制则轮转
            if (files.exists(currentLogFile)) {
                try {
                    // AutoJS6中使用不同的方法获取文件大小
                    var fileStats = new java.io.File(currentLogFile);
                    var fileSize = fileStats.length();
                    if (fileSize >= config.maxFileSize) {
                        rotateLogFiles();
                    }
                } catch (sizeError) {
                    // 如果获取文件大小失败，继续写入，不影响日志功能
                    console.warn('[Logger] 无法获取文件大小:', sizeError.message);
                }
            }
            
            var logLine = JSON.stringify(logEntry) + '\n';
            files.append(currentLogFile, logLine);
        } catch (e) {
            console.error('[Logger] 写入文件失败:', e.message);
        } finally {
            releaseLock('fileLock');
        }
    }
    
    /**
     * 轮转日志文件
     */
    function rotateLogFiles() {
        if (!acquireLock('fileLock', 100)) {
            console.warn('[Logger] 文件轮转锁获取失败，跳过轮转');
            return;
        }
        try {
            // 更新当前日志文件名
            updateCurrentLogFile();
            
            // 清理旧日志文件
            cleanupOldFiles();
        } catch (e) {
            console.error('[Logger] 日志文件轮转失败:', e.message);
        } finally {
            releaseLock('fileLock');
        }
    }
    
    /**
     * 清理旧日志文件
     */
    function cleanupOldFiles() {
        try {
            var logFiles = files.listDir(config.logFilePath, function(name) {
                return name.endsWith('.log');
            });
            
            if (logFiles.length > config.maxFiles) {
                // 按修改时间排序
                logFiles.sort(function(a, b) {
                    try {
                        var pathA = config.logFilePath + a;
                        var pathB = config.logFilePath + b;
                        var fileA = new java.io.File(pathA);
                        var fileB = new java.io.File(pathB);
                        return fileA.lastModified() - fileB.lastModified();
                    } catch (e) {
                        // 如果排序失败，按文件名排序
                        return a.localeCompare(b);
                    }
                });
                
                // 删除最旧的文件
                var filesToDelete = logFiles.length - config.maxFiles;
                for (var i = 0; i < filesToDelete; i++) {
                    files.remove(config.logFilePath + logFiles[i]);
                }
            }
        } catch (e) {
            console.error('[Logger] 清理旧文件失败:', e.message);
        }
    }
    
    /**
     * 线程安全的上传队列操作
     */
    function safeUploadQueuePush(item) {
        if (acquireLock('queueLock', 100)) {
            try {
                uploadQueue.push(item);
                return true;
            } finally {
                releaseLock('queueLock');
            }
        }
        return false;
    }
    
    /**
     * 线程安全的上传队列取出操作
     */
    function safeUploadQueueSplice(count) {
        if (acquireLock('queueLock', 100)) {
            try {
                if (uploadQueue.length === 0) {
                    return [];
                }
                return uploadQueue.splice(0, Math.min(uploadQueue.length, count));
            } finally {
                releaseLock('queueLock');
            }
        }
        return [];
    }
    
    /**
     * 检查是否有有效的认证token
     * @returns {boolean} 是否已登录
     */
    function hasValidToken() {
        try {
            // 使用与httpUtils相同的配置获取token
            var authStorageName = "auth";
            var tokenStorageKey = "cardToken";
            
            var storage = storages.create(authStorageName);
            var token = storage.get(tokenStorageKey, "");
            
            return token && token.trim().length > 0;
        } catch (e) {
            console.warn('[Logger] 检查token状态失败:', e.message);
            return false;
        }
    }
    
    /**
     * 上传日志到服务器
     */
    function uploadLogs() {
        if (uploadQueue.length === 0 || !config.server.url) {
            return;
        }
        
        // 检查是否已登录，未登录时不上传日志
        if (!hasValidToken()) {
            // 清空上传队列，避免积累过多日志
            var droppedCount = uploadQueue.length;
            uploadQueue = [];
            if (droppedCount > 0) {
                debug('Logger', '用户未登录，跳过日志上传，丢弃 ' + droppedCount + ' 条日志');
            }
            return;
        }
        
        var logsToUpload = safeUploadQueueSplice(config.server.batchSize);
        
        if (logsToUpload.length === 0) {
            return;
        }
        
        // 异步上传
        threads.start(function() {
            uploadLogsToServer(logsToUpload);
        });
    }
    
    /**
     * 上传日志到服务器（实际执行）
     */
    function uploadLogsToServer(logs) {
        var retries = 0;
        var success = false;
        
        // 在上传前再次检查登录状态
        if (!hasValidToken()) {
            warn('Logger', '上传过程中发现用户未登录，取消上传');
            return;
        }
        
        while (!success && retries < config.server.maxRetries) {
            try {
                // 导入HTTP工具
                
                var requestData = {
                    logs: logs,
                    deviceId: getDeviceId(),
                    appVersion: getAppVersion(),
                    timestamp: new Date().getTime()
                };
                
                var response = httpUtils.post(config.server.url, requestData, {
                    timeout: config.server.timeout
                    // 加密由全局配置控制，不再强制启用
                });
                
                if (response && response.statusCode === 200) {
                    // 解析JSON响应体
                    try {
                        var result = JSON.parse(response.body);
                        if (result.code === 200) {
                            success = true;
                            debug('Logger', '日志上传成功，条数: ' + logs.length);
                        } else {
                            throw new Error('服务器返回错误: ' + (result.msg || result.message || '未知错误'));
                        }
                    } catch (parseError) {
                        throw new Error('响应解析失败: ' + parseError.message);
                    }
                } else if (response && response.statusCode === 401) {
                    // 认证失败，停止重试
                    error('Logger', '日志上传认证失败，停止重试');
                    break;
                } else {
                    throw new Error('HTTP请求失败，状态码: ' + (response ? response.statusCode : '未知'));
                }
            } catch (e) {
                retries++;
                error('Logger', '日志上传失败 (重试 ' + retries + '/' + config.server.maxRetries + '): ' + e.message);
                
                if (retries < config.server.maxRetries) {
                    // 等待一段时间后重试
                    sleep(1000 * retries);
                }
            }
        }
        
        // 如果上传失败，将日志重新加入队列
        if (!success) {
            uploadQueue = logs.concat(uploadQueue);
            error('Logger', '日志上传彻底失败，已重新加入队列');
        }
    }
    
    /**
     * 获取设备ID
     */
    function getDeviceId() {
        try {
            return device.getAndroidId() || 'unknown';
        } catch (e) {
            return 'unknown';
        }
    }
    
    /**
     * 获取应用版本
     */
    function getAppVersion() {
        try {
            // 这里可以从package.json或其他地方获取版本号
            return '1.0.0';
        } catch (e) {
            return 'unknown';
        }
    }
    
    /**
     * 创建日志条目
     */
    function createLogEntry(level, tag, message, extra) {
        var now = new Date();
        
        // 将 extra 对象转换为 JSON 字符串，如果转换失败则保持原值
        var extraString = null;
        if (extra !== null && extra !== undefined) {
            try {
                if (typeof extra === 'object') {
                    extraString = JSON.stringify(extra);
                } else {
                    extraString = String(extra);
                }
            } catch (e) {
                extraString = String(extra);
            }
        }
        
        return {
            timestamp: now.toISOString(),
            level: LogLevelNames[level],
            levelValue: level,
            tag: tag || 'Default',
            message: message || '',
            extra: extraString,
            deviceId: getDeviceId(),
            appVersion: getAppVersion()
        };
    }
    
    /**
     * 记录日志（通用方法）
     */
    function log(level, tag, message, extra) {
        if (!initialized) {
            init(); // 自动初始化
        }
        
        // 检查是否需要输出到控制台或文件
        var shouldOutput = level >= config.level;
        // 检查是否需要上传到服务器
        var shouldUpload = level >= config.serverLevel && config.serverUpload;
        
        // 如果既不需要输出也不需要上传，直接返回
        if (!shouldOutput && !shouldUpload) {
            return;
        }
        
        var logEntry = createLogEntry(level, tag, message, extra);
        
        // 如果需要本地输出（控制台或文件），加入处理队列
        if (shouldOutput) {
            safeQueuePush(logEntry);
        }
        
        // 如果需要上传到服务器，单独加入上传队列
        if (shouldUpload) {
            safeUploadQueuePush(logEntry);
        }
    }
    
    /**
     * DEBUG级别日志
     */
    function debug(tag, message, extra) {
        log(LogLevel.DEBUG, tag, message, extra);
    }
    
    /**
     * INFO级别日志
     */
    function info(tag, message, extra) {
        log(LogLevel.INFO, tag, message, extra);
    }
    
    /**
     * WARN级别日志
     */
    function warn(tag, message, extra) {
        log(LogLevel.WARN, tag, message, extra);
    }
    
    /**
     * ERROR级别日志
     */
    function error(tag, message, extra) {
        log(LogLevel.ERROR, tag, message, extra);
    }
    
    /**
     * 设置配置
     */
    function setConfig(key, value) {
        if (key === 'server' && typeof value === 'object') {
            Object.assign(config.server, value);
        } else {
            config[key] = value;
        }
        
        // 如果更新了服务器配置且启用了上传，重新启动上传服务
        if (key === 'serverUpload' && value && config.server.url) {
            startUploadService();
        } else if (key === 'server' && config.serverUpload && value.url) {
            startUploadService();
        }
    }
    
    /**
     * 获取配置
     */
    function getConfig(key) {
        return key ? config[key] : config;
    }
    
    /**
     * 刷新日志（立即处理队列中的所有日志）
     */
    function flush() {
        while (logQueue.length > 0) {
            processLogQueue();
            sleep(10);
        }
        
        // 如果启用了上传，也立即上传
        if (config.serverUpload && uploadQueue.length > 0) {
            uploadLogs();
        }
    }
    
    /**
     * 销毁日志管理器
     */
    function destroy() {
        if (processTimer) {
            clearInterval(processTimer);
            processTimer = null;
        }
        
        if (uploadTimer) {
            clearInterval(uploadTimer);
            uploadTimer = null;
        }
        
        // 处理剩余的日志
        flush();
        
        initialized = false;
        info('Logger', '日志管理器已销毁');
    }
    
    /**
     * 获取本地日志文件列表
     */
    function getLogFiles() {
        try {
            return files.listDir(config.logFilePath, function(name) {
                return name.endsWith('.log');
            }).map(function(name) {
                var path = config.logFilePath + name;
                var fileInfo = {
                    name: name,
                    path: path,
                    size: 0,
                    lastModified: 0
                };
                
                try {
                    var file = new java.io.File(path);
                    fileInfo.size = file.length();
                    fileInfo.lastModified = file.lastModified();
                } catch (e) {
                    // 如果获取文件信息失败，使用默认值
                    console.warn('[Logger] 无法获取文件信息:', name, e.message);
                }
                
                return fileInfo;
            });
        } catch (e) {
            error('Logger', '获取日志文件列表失败: ' + e.message);
            return [];
        }
    }
    
    /**
     * 读取日志文件内容
     */
    function readLogFile(filename) {
        try {
            var filePath = config.logFilePath + filename;
            if (!files.exists(filePath)) {
                return null;
            }
            
            var content = files.read(filePath);
            var lines = content.split('\n').filter(function(line) {
                return line.trim();
            });
            
            return lines.map(function(line) {
                try {
                    return JSON.parse(line);
                } catch (e) {
                    return { message: line, timestamp: '', level: 'UNKNOWN' };
                }
            });
        } catch (e) {
            error('Logger', '读取日志文件失败: ' + e.message);
            return null;
        }
    }
    
    // 公开API
    return {
        // 日志级别常量
        LogLevel: LogLevel,
        
        // 初始化和配置
        init: init,
        setConfig: setConfig,
        getConfig: getConfig,
        
        // 日志记录方法
        debug: debug,
        info: info,
        warn: warn,
        error: error,
        
        // 工具方法
        flush: flush,
        destroy: destroy,
        getLogFiles: getLogFiles,
        readLogFile: readLogFile
    };
}

// 导出模块
module.exports = Logger;