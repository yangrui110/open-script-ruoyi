/**
 * 日志集成示例
 * 展示如何在AutoJS脚本中集成和使用日志功能
 */

// 导入必要的模块
var Logger = require('./logger.js');
var config = require('./config.js');

/**
 * 应用日志管理器
 * 封装了日志功能，提供更便捷的使用方式
 */
function AppLogger() {
    var logger = new Logger();
    var initialized = false;
    
    /**
     * 初始化应用日志
     */
    function init() {
        if (initialized) return;
        
        try {
            // 使用配置文件中的日志配置
            var loggerConfig = config.getLoggerConfig();
            
            // 初始化日志管理器
            logger.init(loggerConfig);
            initialized = true;
            
            // 记录应用启动日志
            logger.info('AppLogger', '应用日志系统初始化完成', {
                version: getAppVersion(),
                deviceInfo: getDeviceInfo(),
                startTime: new Date().toISOString()
            });
            
        } catch (e) {
            console.error('日志系统初始化失败:', e.message);
        }
    }
    
    /**
     * 判断是否为生产模式
     */
    function isProductionMode() {
        // 使用配置文件中的环境检测
        return config.getEnvironment() === 'production';
    }
    
    /**
     * 获取应用版本
     */
    function getAppVersion() {
        try {
            // 从package.json读取版本号
            var packagePath = files.path('./package.json');
            if (files.exists(packagePath)) {
                var packageContent = files.read(packagePath);
                var packageJson = JSON.parse(packageContent);
                return packageJson.version || config.APP_VERSION;
            }
        } catch (e) {
            // 忽略错误
        }
        return config.APP_VERSION;
    }
    
    /**
     * 获取设备信息
     */
    function getDeviceInfo() {
        try {
            return {
                model: device.model,
                brand: device.brand,
                release: device.release,
                sdk: device.sdkInt,
                width: device.width,
                height: device.height
            };
        } catch (e) {
            return { error: '无法获取设备信息' };
        }
    }
    
    /**
     * 记录用户操作日志
     */
    function logUserAction(action, details) {
        logger.info('UserAction', action, details);
    }
    
    /**
     * 记录性能日志
     */
    function logPerformance(operation, duration, details) {
        var level = duration > 5000 ? 'warn' : 'info'; // 超过5秒记录为警告
        logger[level]('Performance', operation + ' 耗时 ' + duration + 'ms', 
            Object.assign({ duration: duration }, details || {}));
    }
    
    /**
     * 记录错误日志
     */
    function logError(error, context) {
        var errorInfo = {
            message: error.message || error.toString(),
            stack: error.stack,
            context: context || 'unknown'
        };
        
        logger.error('Error', '发生错误: ' + errorInfo.message, errorInfo);
    }
    
    /**
     * 记录网络请求日志
     */
    function logNetworkRequest(url, method, status, duration, details) {
        var level = status >= 400 ? 'error' : (status >= 300 ? 'warn' : 'info');
        logger[level]('Network', method + ' ' + url + ' -> ' + status, {
            url: url,
            method: method,
            status: status,
            duration: duration,
            details: details
        });
    }
    
    /**
     * 记录脚本执行日志
     */
    function logScriptExecution(scriptName, action, result, details) {
        logger.info('ScriptExecution', scriptName + ' - ' + action, {
            script: scriptName,
            action: action,
            result: result,
            details: details
        });
    }
    
    /**
     * 记录调试信息
     */
    function debug(tag, message, extra) {
        logger.debug(tag, message, extra);
    }
    
    /**
     * 记录一般信息
     */
    function info(tag, message, extra) {
        logger.info(tag, message, extra);
    }
    
    /**
     * 记录警告信息
     */
    function warn(tag, message, extra) {
        logger.warn(tag, message, extra);
    }
    
    /**
     * 记录错误信息
     */
    function error(tag, message, extra) {
        logger.error(tag, message, extra);
    }
    
    /**
     * 性能监控装饰器
     */
    function withPerformanceLogging(func, operationName) {
        return function() {
            var startTime = new Date().getTime();
            var result;
            var error = null;
            
            try {
                result = func.apply(this, arguments);
            } catch (e) {
                error = e;
                throw e;
            } finally {
                var duration = new Date().getTime() - startTime;
                if (error) {
                    logError(error, operationName);
                } else {
                    logPerformance(operationName, duration);
                }
            }
            
            return result;
        };
    }
    
    /**
     * 错误捕获装饰器
     */
    function withErrorLogging(func, context) {
        return function() {
            try {
                return func.apply(this, arguments);
            } catch (e) {
                logError(e, context);
                throw e;
            }
        };
    }
    
    /**
     * 清理和销毁
     */
    function destroy() {
        if (initialized) {
            logger.info('AppLogger', '应用日志系统正在关闭');
            logger.flush();
            logger.destroy();
            initialized = false;
        }
    }
    
    // 自动初始化
    init();
    
    // 公开API
    return {
        // 基础日志方法
        debug: debug,
        info: info,
        warn: warn,
        error: error,
        
        // 专用日志方法
        logUserAction: logUserAction,
        logPerformance: logPerformance,
        logError: logError,
        logNetworkRequest: logNetworkRequest,
        logScriptExecution: logScriptExecution,
        
        // 装饰器方法
        withPerformanceLogging: withPerformanceLogging,
        withErrorLogging: withErrorLogging,
        
        // 工具方法
        destroy: destroy,
        
        // 原始logger访问（高级用法）
        getRawLogger: function() { return logger; }
    };
}

// 创建全局应用日志实例
var appLogger = new AppLogger();

// 全局错误处理
if (typeof events !== 'undefined') {
    events.on('uncaught_exception', function(e) {
        appLogger.logError(e, 'UncaughtException');
    });
}

// 导出实例而不是构造函数，使用单例模式
module.exports = appLogger;