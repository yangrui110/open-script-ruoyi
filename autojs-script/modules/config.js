/**
 * 应用配置管理模块
 * 统一管理API配置、日志配置等所有配置项
 * 
 * 多租户配置说明：
 * - TENANT_ID: 租户标识符，用于多租户数据隔离
 * - 不同的客户端应该配置不同的TENANT_ID值
 * - 默认值"000000"是系统默认租户，实际部署时请根据业务需要修改
 * - 租户ID决定了该客户端只能访问对应租户下的卡密数据
 */

// API配置
let API_CONFIG = {
    // 基础URL，实际部署时请修改为正确的服务器地址
    BASE_URL: "http://192.168.201.7:8080",
    CLIENT_ID: "f36c69cd4655566bbfac652e479cb931",
    
    // 加密配置
    ENCRYPTION: {
        // 是否启用加密
        ENABLED: true,
        // 加密头部标识
        HEADER_FLAG: "encrypt-key",
        // RSA公钥（用于加密AES密钥发送给服务器）
        // 注意：这是前端加密用的公钥，对应后端配置中的请求解密私钥
        PUBLIC_KEY: "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKoR8mX0rGKLqzcWmOzbfj64K8ZIgOdHnzkXSOVOZbFu/TJhZ7rFAN+eaGkl3C4buccQd/EjEsj9ir7ijT7h96MCAwEAAQ==",
        // RSA私钥（用于解密服务器响应）
        // 注意：这是前端解密用的私钥，对应后端配置中的响应加密公钥
        PRIVATE_KEY: "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmc3CuPiGL/LcIIm7zryCEIbl1SPzBkr75E2VMtxegyZ1lYRD+7TZGAPkvIsBcaMs6Nsy0L78n2qh+lIZMpLH8wIDAQABAkEAk82Mhz0tlv6IVCyIcw/s3f0E+WLmtPFyR9/WtV3Y5aaejUkU60JpX4m5xNR2VaqOLTZAYjW8Wy0aXr3zYIhhQQIhAMfqR9oFdYw1J9SsNc+CrhugAvKTi0+BF6VoL6psWhvbAiEAxPPNTmrkmrXwdm/pQQu3UOQmc2vCZ5tiKpW10CgJi8kCIFGkL6utxw93Ncj4exE/gPLvKcT+1Emnoox+O9kRXss5AiAMtYLJDaLEzPrAWcZeeSgSIzbL+ecokmFKSDDcRske6QIgSMkHedwND1olF8vlKsJUGK3BcdtM8w4Xq7BpSBwsloE="
    },
    // 租户ID，用于多租户隔离（写死配置，不同的客户端使用不同的租户ID）
    // 重要提示：
    // 1. 每个客户端应该配置唯一的租户ID
    // 2. 租户ID应与服务端sys_tenant表中的tenant_id字段对应
    // 3. 修改此配置后，只能访问对应租户下的卡密数据
    // 4. 常见租户ID格式：6位数字，如：000000（默认）、000001、000002等
    TENANT_ID: "205493", // 默认租户ID，实际部署时请修改为对应的租户ID
    ENDPOINTS: {
        LOGIN: "/open-api/script/login",
        LOGOUT: "/open-api/script/logout", 
        CARD_INFO: "/open-api/script/card-info",
        VERIFY: "/open-api/script/verify",
        LATEST_VERSION: "/open-api/script/latest",
        GAME_DATA: "/open-api/script/game-data",
        LOGS_UPLOAD: "/open-api/script/logs/upload"
    },
    // HTTP请求配置
    REQUEST_CONFIG: {
        TIMEOUT: 30000,
        MAX_RETRIES: 3
    }
};

// 日志配置
let LOGGER_CONFIG = {
    // 日志级别 (DEBUG: 0, INFO: 1, WARN: 2, ERROR: 3)
    LEVEL: {
        DEBUG: 0,
        INFO: 1,
        WARN: 2,
        ERROR: 3
    },
    
    // 默认配置
    DEFAULT: {
        level: 0, // 控制台和文件输出的最低级别（DEBUG级别）
        serverLevel: 1, // 服务器上传的最低级别（INFO级别）
        
        // 本地文件配置
        logFilePath: '/sdcard/autojs-logs/',
        maxFileSize: 5 * 1024 * 1024, // 5MB
        maxFiles: 10,
        
        // 输出配置
        consoleOutput: true,  // 控制台输出
        fileOutput: true,     // 本地文件存储
        serverUpload: false,  // 默认禁用服务器上传，需要在登录后手动启用
        
        // 服务器配置
        server: {
            url: API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.LOGS_UPLOAD,
            batchSize: 50,
            uploadInterval: 5000, // 5秒上传一次
            maxRetries: 3,
            timeout: 30000
        }
    },
    
    // 生产环境配置
    PRODUCTION: {
        level: 2, // 控制台和文件输出只记录WARN和ERROR
        serverLevel: 1, // 服务器上传INFO及以上级别
        consoleOutput: false, // 生产模式不输出到控制台
        fileOutput: true,
        serverUpload: false // 生产环境默认禁用服务器上传，需要在登录后启用
    },
    
    // 开发环境配置
    DEVELOPMENT: {
        level: 0, // DEBUG级别，允许所有日志输出到控制台和文件
        serverLevel: 1, // 服务器上传只发送INFO及以上级别（DEBUG不上传）
        consoleOutput: true,
        fileOutput: true,
        serverUpload: false // 开发环境默认不上传到服务器，需要在登录后启用
    }
};

// 应用配置
let APP_CONFIG = {
    // 应用信息
    APP_INFO: {
        NAME: "AutoJS脚本管理器",
        VERSION: "1.0.0",
        DEVELOPER: "闪灵科技有限公司",
        UPDATE_DATE: "2025-08-24"
    },
    
    // 存储键名
    STORAGE_KEYS: {
        AUTH: "auth",
        LOGIN: "login",
        CARD_TOKEN: "cardToken",
        CARD_NO: "cardNo",
        DEVICE_ID: "deviceId",
        REMEMBER: "remember"
    },
    
    // 界面配置
    UI_CONFIG: {
        // 主题色
        PRIMARY_COLOR: "#4CAF50",
        ERROR_COLOR: "#f44336",
        WARNING_COLOR: "#FF9800",
        INFO_COLOR: "#2196F3",
        
        // 动画配置
        ANIMATION_DURATION: 200,
        TOAST_DURATION: 2000,
        
        // 悬浮窗配置
        FLOATY: {
            MAIN_BUTTON_SIZE: 48, // dp
            ACTION_BUTTON_SIZE: 36, // dp
            MENU_RADIUS: 120, // 菜单半径
            DRAG_THRESHOLD: 10, // 拖动阈值
            CLICK_TIMEOUT: 300, // 点击超时时间
            SNAP_ANIMATION_DURATION: 200,
            AUTO_CLOSE_DELAY: 10000 // 控制台自动关闭延迟
        }
    },
    
    // 脚本配置
    SCRIPT_CONFIG: {
        // 脚本目录
        SCRIPTS_DIR: "/sdcard/AutoJS脚本/",
        
        // 脚本文件名优先级
        START_SCRIPT_NAMES: [
            'start.js',
            'main.js',
            'index.js',
            'app.js',
            'script.js'
        ],
        
        // 版本文件名
        VERSION_FILE_NAME: "version.json",
        
        // 文件类型
        FILE_TYPES: {
            JS: 0,
            ZIP: 1
        }
    },
    
    // 设备配置
    DEVICE_CONFIG: {
        // 权限检查间隔
        PERMISSION_CHECK_INTERVAL: 3000,
        
        // 默认设备信息
        DEFAULT_DEVICE_INFO: {
            deviceWidth: 1080,
            deviceHeight: 1920,
            deviceModel: "Unknown Device",
            deviceInfo: "Unknown Device"
        }
    }
};

// 环境检测
function getEnvironment() {
    try {
        // 检查生产环境标志文件
        if (files.exists('/sdcard/production.flag')) {
            return 'production';
        }
        
        // 检查开发环境标志
        if (files.exists('/sdcard/development.flag')) {
            return 'development';
        }
        
        // 默认为开发环境
        return 'development';
    } catch (e) {
        return 'development';
    }
}

// 获取当前环境的日志配置
function getLoggerConfig() {
    let env = getEnvironment();
    let baseConfig = Object.assign({}, LOGGER_CONFIG.DEFAULT);
    
    if (env === 'production') {
        Object.assign(baseConfig, LOGGER_CONFIG.PRODUCTION);
    } else {
        Object.assign(baseConfig, LOGGER_CONFIG.DEVELOPMENT);
    }
    
    return baseConfig;
}

// 配置验证函数
function validateConfig() {
    let errors = [];
    
    // 验证API配置
    if (!API_CONFIG.BASE_URL) {
        errors.push("API_CONFIG.BASE_URL 不能为空");
    }
    
    if (!API_CONFIG.CLIENT_ID) {
        errors.push("API_CONFIG.CLIENT_ID 不能为空");
    }
    
    // 验证日志配置
    let logConfig = getLoggerConfig();
    if (!logConfig.logFilePath) {
        errors.push("日志文件路径不能为空");
    }
    
    if (logConfig.maxFileSize <= 0) {
        errors.push("日志文件最大大小必须大于0");
    }
    
    return {
        isValid: errors.length === 0,
        errors: errors
    };
}

// 导出配置对象
module.exports = {
    // 主要配置
    API_CONFIG: API_CONFIG,
    LOGGER_CONFIG: LOGGER_CONFIG,
    APP_CONFIG: APP_CONFIG,
    
    // 工具函数
    getEnvironment: getEnvironment,
    getLoggerConfig: getLoggerConfig,
    validateConfig: validateConfig,
    
    // 便捷访问
    get BASE_URL() { return API_CONFIG.BASE_URL; },
    get CLIENT_ID() { return API_CONFIG.CLIENT_ID; },
    get TENANT_ID() { return API_CONFIG.TENANT_ID; },
    get ENDPOINTS() { return API_CONFIG.ENDPOINTS; },
    get APP_NAME() { return APP_CONFIG.APP_INFO.NAME; },
    get APP_VERSION() { return APP_CONFIG.APP_INFO.VERSION; },
    get SCRIPTS_DIR() { return APP_CONFIG.SCRIPT_CONFIG.SCRIPTS_DIR; },
    get STORAGE_KEYS() { return APP_CONFIG.STORAGE_KEYS; }
}; 