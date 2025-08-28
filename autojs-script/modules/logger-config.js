let logger = require('./logger.js');

/**
 * Logger 配置示例
 * 展示如何配置日志上传到服务器
 */

// 创建 Logger 实例
let loggerInstance = new logger();

// 配置日志管理器
loggerInstance.init({
    // 日志级别 (DEBUG: 0, INFO: 1, WARN: 2, ERROR: 3)
    level: loggerInstance.LogLevel.INFO,
    
    // 本地文件配置
    logFilePath: '/sdcard/autojs-logs/',
    maxFileSize: 5 * 1024 * 1024, // 5MB
    maxFiles: 10,
    
    // 输出配置
    consoleOutput: true,  // 控制台输出
    fileOutput: true,     // 本地文件存储
    serverUpload: true,   // 启用服务器上传
    
    // 服务器配置
    server: {
        url: 'http://192.168.201.7:8080/open-api/script/logs/upload', // 修改为你的服务器地址
        batchSize: 50,        // 批量上传条数
        uploadInterval: 5000, // 30秒上传一次
        maxRetries: 3,        // 最大重试次数
        timeout: 30000        // 30秒超时
    }
});

// 使用示例
// loggerInstance.info('Example', '这是一条测试日志');
// loggerInstance.warn('Example', '这是一条警告日志', { extra: 'data' });
// loggerInstance.error('Example', '这是一条错误日志', { error: 'details' });

// 导出配置好的 logger 实例
module.exports = loggerInstance; 