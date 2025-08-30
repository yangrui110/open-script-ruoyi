"ui";

// 导入配置模块
let config = require('./modules/config.js');
// 导入HTTP工具模块
let httpUtils = require('./modules/http-utils.js');
// 导入日志模块 - 使用单例模式的logger实例
let logger = require('./modules/logger-integration.js');

// 初始化logger配置
let loggerConfig = config.getLoggerConfig();
logger.getRawLogger().setConfig('server', loggerConfig.server);
// 初始时禁用服务器上传，等用户登录后再启用
logger.getRawLogger().setConfig('serverUpload', false);

// 使用配置文件中的API配置
let API_CONFIG = config.API_CONFIG;

// 简化的API工具函数（认证逻辑已移至 http-utils.js）
let apiUtils = {
    // 发送GET请求
    get: function(endpoint, additionalHeaders) {
        let startTime = new Date().getTime();
        let options = { headers: additionalHeaders };
        let response = httpUtils.get(API_CONFIG.BASE_URL + endpoint, options);
        let duration = new Date().getTime() - startTime;
        
        // 获取认证头部用于日志记录
        let authHeaders = httpUtils.getAuthHeaders();
        logger.logNetworkRequest(endpoint, "GET", response.statusCode, duration, {
            hasAuth: !!authHeaders['Authorization']
        });
        
        return response;
    },
    
    // 发送POST请求
    post: function(endpoint, data, additionalHeaders) {
        let startTime = new Date().getTime();
        let options = { headers: additionalHeaders };
        let response = httpUtils.post(API_CONFIG.BASE_URL + endpoint, data, options);
        let duration = new Date().getTime() - startTime;
        
        // 获取认证头部用于日志记录
        let authHeaders = httpUtils.getAuthHeaders();
        logger.logNetworkRequest(endpoint, "POST", response.statusCode, duration, {
            hasAuth: !!authHeaders['Authorization'],
            dataSize: data ? JSON.stringify(data).length : 0,
            requestData:JSON.stringify(data),
            responseData:JSON.stringify(response)
        });
        
        return response;
    },
    
    // 不需要token的请求（如登录接口）
    postWithoutAuth: function(endpoint, data, additionalHeaders) {
        let startTime = new Date().getTime();
        let options = { 
            skipAuth: true, 
            headers: additionalHeaders 
        };
        let response = httpUtils.post(API_CONFIG.BASE_URL + endpoint, data, options);
        let duration = new Date().getTime() - startTime;
        
        logger.logNetworkRequest(endpoint, "POST", response.statusCode, duration, {
            hasAuth: false,
            dataSize: data ? JSON.stringify(data).length : 0,
            type: "withoutAuth"
        });
        
        return response;
    },
    
    // 统一的错误处理
    handleApiError: function(response, context) {
        if (response.statusCode === 401) {
            // token过期或无效
            logger.warn("Auth", "认证失败，清除token并跳转到登录页");
            storages.create("auth").remove("cardToken");
            // 禁用日志上传功能
            logger.getRawLogger().setConfig('serverUpload', false);
            if (isLoggedIn) {
                ui.run(() => {
                    performLogout();
                    toast("登录已过期，请重新登录");
                });
            }
            return true; // 表示处理了认证错误
        } else if (response.statusCode === 403) {
            // 权限不足
            ui.run(() => {
                toast("权限不足");
            });
            return true;
        } else if (response.statusCode >= 500) {
            // 服务器错误
            ui.run(() => {
                toast("服务器错误，请稍后重试");
            });
            return true;
        }
        return false; // 没有处理错误
    }
};

// 全局变量
let isLoggedIn = false;
let userInfo = {};
let gamesList = []; // 可用游戏列表
let selectedGameId = null; // 当前选中的游戏ID
let currentRunningScript = null; // 当前运行的脚本引擎

// 界面布局
ui.layout(
    <drawer id="drawer">
        {/* 主要内容 */}
        <vertical bg="#ffffff">
                    {/* 头部标题栏 */}
        <horizontal bg="#4CAF50" h="56dp" gravity="center_vertical">
            <button id="menuButton" text="☰" textSize="20sp" textColor="#ffffff" 
                    bg="?attr/selectableItemBackgroundBorderless" w="56dp" h="56dp" 
                    gravity="center" style="?android:attr/borderlessButtonStyle"/>
            <text text="AutoJS脚本管理器" textSize="20sp" textColor="#ffffff" 
                  textStyle="bold" layout_weight="1" gravity="center_vertical"/>
        </horizontal>
        
        {/* 主要内容区域 */}
        <frame id="mainFrame" layout_weight="1">
            
            {/* 登录界面 */}
            <vertical id="loginPage" padding="20dp" visibility="visible">
                <card w="*" h="auto" margin="10dp" cardCornerRadius="8dp" cardElevation="4dp">
                    <vertical padding="20dp">
                        {/* 登录标题 */}
                        <text text="用户登录" textSize="24sp" textColor="#333333" textStyle="bold" 
                              gravity="center" margin="0 0 20dp 0"/>
                        
                                                 {/* Logo或图标 */}
                         <text text="👤" textSize="48sp" textColor="#4CAF50" 
                               layout_gravity="center" margin="0 0 20dp 0"/>
                        
                        {/* 卡密输入框 */}
                        <text text="卡密" textSize="16sp" textColor="#666666" margin="0 0 5dp 0"/>
                        <input id="cardNoInput" hint="请输入卡密" 
                               textSize="16sp" h="48dp" bg="#F5F5F5"
                               padding="12dp" margin="0 0 15dp 0"/>
                        
                        {/* 设备ID输入框（可选） */}
                        <text text="设备ID（可选）" textSize="16sp" textColor="#666666" margin="0 0 5dp 0"/>
                        <input id="deviceIdInput" hint="留空将自动获取" 
                               textSize="16sp" h="48dp" bg="#F5F5F5"
                               padding="12dp" margin="0 0 20dp 0"/>
                        
                        {/* 记住卡密选项 */}
                        <horizontal gravity="left">
                            <checkbox id="rememberCard" text="记住卡密" textSize="14sp" textColor="#666666"/>
                        </horizontal>
                        
                        {/* 登录按钮 */}
                        <button id="loginBtn" text="登录" textSize="18sp" 
                                bg="#4CAF50" textColor="#ffffff" h="48dp" 
                                margin="20dp 0 10dp 0" style="Widget.AppCompat.Button.Colored"/>
                        
                        {/* 状态提示 */}
                        <text id="statusText" text="" textSize="14sp" textColor="#f44336" 
                              gravity="center" margin="10dp 0 0 0" visibility="gone"/>
                        
                        {/* 加载进度条 */}
                        <progressbar id="loadingProgress" style="@android:style/Widget.ProgressBar.Horizontal"
                                   indeterminate="true" visibility="gone" margin="10dp 0 0 0"/>
                    </vertical>
                </card>
                
                {/* 底部信息 */}
                <text text={config.APP_NAME + " v" + config.APP_VERSION} textSize="12sp" textColor="#999999" 
                      gravity="center" margin="20dp 0 0 0"/>
            </vertical>
            
            {/* 主页面（登录成功后显示） */}
            <scroll id="homePage" visibility="gone">
                <vertical padding="20dp">
                    <card w="*" h="auto" margin="10dp" cardCornerRadius="8dp" cardElevation="4dp">
                        <vertical padding="20dp">
                        {/* 欢迎信息 */}
                        <text text="登录成功" textSize="24sp" textColor="#4CAF50" textStyle="bold" 
                              gravity="center" margin="0 0 20dp 0"/>
                        
                                                 {/* 用户头像 */}
                         <text text="👨‍💻" textSize="64sp" textColor="#4CAF50" 
                               layout_gravity="center" margin="0 0 20dp 0"/>
                        
                        {/* 用户信息显示 */}
                        <text text="用户信息" textSize="18sp" textColor="#333333" textStyle="bold" 
                              margin="0 0 15dp 0"/>
                        
                        {/* 卡密 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="卡密：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="displayCardNo" text="" textSize="16sp" textColor="#333333" textStyle="bold"/>
                        </horizontal>
                        
                        {/* 剩余天数 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="剩余天数：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="remainingDays" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 过期时间 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="过期时间：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="expireTime" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 登录时间 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="登录时间：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="loginTime" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 设备信息 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="设备型号：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="deviceModel" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 系统版本 */}
                        <horizontal margin="0 0 10dp 0">
                            <text text="系统版本：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <text id="systemVersion" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 可绑定设备数 */}
                        <horizontal margin="0 0 20dp 0">
                            <text text="可绑定设备：" textSize="16sp" textColor="#666666" w="120dp"/>
                            <text id="deviceSize" text="" textSize="16sp" textColor="#333333"/>
                        </horizontal>
                        
                        {/* 游戏脚本选择 */}
                        <text text="游戏脚本" textSize="18sp" textColor="#333333" textStyle="bold" 
                              margin="0 20dp 15dp 0"/>
                        
                        {/* 游戏选择下拉框 */}
                        <horizontal margin="0 0 15dp 0">
                            <text text="选择游戏：" textSize="16sp" textColor="#666666" w="80dp"/>
                            <spinner id="gameSpinner" entries="[]" textSize="16sp" 
                                    layout_weight="1" margin="0 0 10dp 0"/>
                        </horizontal>
                        
                                                 {/* 启动脚本按钮 - 使用权重布局强制对齐 */}
                         <horizontal margin="0 0 20dp 0" weightSum="2" gravity="center">
                             <button id="startScriptBtn" text="启动脚本" textSize="16sp" 
                                     bg="#FF9800" textColor="#ffffff" h="40dp" 
                                     layout_weight="1" w="0dp" margin="0 5dp 0 0" 
                                     layout_gravity="center" style="Widget.AppCompat.Button.Colored"/>
                             <button id="updateScriptBtn" text="更新脚本" textSize="16sp" 
                                     bg="#9C27B0" textColor="#ffffff" h="40dp" 
                                     layout_weight="1" w="0dp" margin="0 0 0 5dp" 
                                     layout_gravity="center" style="Widget.AppCompat.Button.Colored"/>
                         </horizontal>
                        
                                                 {/* 功能按钮 - 使用权重布局强制对齐 */}
                         <horizontal margin="20dp 0 0 0" weightSum="2" gravity="center">
                             <button id="logoutBtn" text="退出登录" textSize="16sp" 
                                     bg="#f44336" textColor="#ffffff" h="40dp" 
                                     layout_weight="1" w="0dp" margin="0 5dp 0 0" 
                                     layout_gravity="center" style="Widget.AppCompat.Button.Colored"/>
                             <button id="refreshBtn" text="刷新信息" textSize="16sp" 
                                     bg="#2196F3" textColor="#ffffff" h="40dp" 
                                     layout_weight="1" w="0dp" margin="0 0 0 5dp" 
                                     layout_gravity="center" style="Widget.AppCompat.Button.Colored"/>
                         </horizontal>
                    </vertical>
                </card>
                </vertical>
            </scroll>
            
        </frame>
        </vertical>
        
        {/* 左侧抽屉菜单 */}
        <vertical bg="#ffffff" layout_gravity="left" w="280dp">
            {/* 抽屉头部 */}
            <vertical bg="#4CAF50" h="160dp" padding="20dp" gravity="bottom">
                <text text="📱" textSize="48sp" textColor="#ffffff" 
                      layout_gravity="center" margin="0 0 10dp 0"/>
                <text text={config.APP_NAME} textSize="18sp" textColor="#ffffff" 
                      textStyle="bold" layout_gravity="center"/>
                <text text={"v" + config.APP_VERSION} textSize="14sp" textColor="#E8F5E8" 
                      layout_gravity="center" margin="5dp 0 0 0"/>
            </vertical>
            
            {/* 菜单项列表 */}
            <vertical h="*" padding="8dp">
                {/* 无障碍服务开关 */}
                <horizontal id="accessibilityItem" h="56dp" margin="2dp" padding="12dp 16dp" 
                           gravity="center_vertical" bg="?attr/selectableItemBackground">
                    <text text="♿" textSize="20sp" textColor="#4CAF50" w="32dp" gravity="center" 
                          layout_gravity="center_vertical"/>
                    <text text="无障碍服务" textSize="16sp" textColor="#333333" layout_weight="1" 
                          margin="12dp 0 0 0" layout_gravity="center_vertical"/>
                    <Switch id="accessibilitySwitch" layout_width="wrap_content" layout_height="wrap_content"
                            layout_gravity="center_vertical"/>
                </horizontal>
                
                {/* 浮动窗口开关 */}
                <horizontal id="floatyItem" h="56dp" margin="2dp" padding="12dp 16dp" 
                           gravity="center_vertical" bg="?attr/selectableItemBackground">
                    <text text="🌐" textSize="20sp" textColor="#2196F3" w="32dp" gravity="center" 
                          layout_gravity="center_vertical"/>
                    <text text="浮动窗口" textSize="16sp" textColor="#333333" layout_weight="1" 
                          margin="12dp 0 0 0" layout_gravity="center_vertical"/>
                    <Switch id="floatySwitch" layout_width="wrap_content" layout_height="wrap_content"
                            layout_gravity="center_vertical"/>
                </horizontal>
                
                {/* 关于按钮 */}
                <button id="menuAbout" text="ℹ️ 关于" textSize="16sp" textColor="#333333" 
                        bg="#ffffff" h="56dp" margin="2dp" padding="12dp 16dp" gravity="left|center_vertical" 
                        style="?android:attr/borderlessButtonStyle"/>
                        
                {/* 退出按钮 */}
                <button id="menuLogout" text="🚪 退出" textSize="16sp" textColor="#333333" 
                        bg="#ffffff" h="56dp" margin="2dp" padding="12dp 16dp" gravity="left|center_vertical" 
                        style="?android:attr/borderlessButtonStyle"/>
            </vertical>
            
            {/* 抽屉底部 */}
            <vertical bg="#F5F5F5" h="80dp" padding="20dp" gravity="center">
                            <text text={"© 2024 " + config.APP_CONFIG.APP_INFO.DEVELOPER} textSize="12sp" textColor="#999999" 
                  layout_gravity="center"/>
            </vertical>
        </vertical>
    </drawer>
);

// 输入框样式已通过XML直接设置

// 菜单功能已通过按钮实现，不再需要数据数组

// 初始化菜单按钮事件
function initMenuButtons() {
    // 无障碍开关事件
    ui.accessibilitySwitch.setOnCheckedChangeListener((view, isChecked) => {
        toggleAccessibilityService(isChecked);
    });
    
    // 无障碍区域点击事件（点击整个区域也能切换开关）
    ui.accessibilityItem.click(() => {
        ui.accessibilitySwitch.setChecked(!ui.accessibilitySwitch.isChecked());
    });
    
    // 浮动窗口开关事件
    ui.floatySwitch.setOnCheckedChangeListener((view, isChecked) => {
        toggleFloatyService(isChecked);
    });
    
    // 浮动窗口区域点击事件（点击整个区域也能切换开关）
    ui.floatyItem.click(() => {
        ui.floatySwitch.setChecked(!ui.floatySwitch.isChecked());
    });
    
    ui.menuAbout.click(() => {
        handleMenuItemClick("about");
        closeDrawer();
    });
    
    ui.menuLogout.click(() => {
        handleMenuItemClick("logout");
        closeDrawer();
    });
}

// 验证现有token是否有效
function verifyExistingToken() {
    let cardToken = storages.create("auth").get("cardToken", "");
    if (!cardToken) {
        logger.debug("Auth", "没有保存的token");
        return;
    }
    
    logger.info("Auth", "检查现有token有效性...");
    threads.start(() => {
        try {
            let response = apiUtils.get(API_CONFIG.ENDPOINTS.CARD_INFO);
            
            if (response.statusCode === 200) {
                let result = JSON.parse(response.body);
                if (result.code === 200 && result.data) {
                    // token有效，自动登录
                    userInfo = result.data;
                    userInfo.loginTimeDisplay = new Date().toLocaleString();
                    
                    // 启用日志上传功能
                    logger.getRawLogger().setConfig('serverUpload', true);
                    logger.info("Auth", "自动登录成功，已启用日志上传功能", { cardNo: userInfo.cardNo, remainingDays: userInfo.remainingDays });
                    
                    ui.run(() => {
                        showLoginSuccess();
                        toast("自动登录成功");
                    });
                } else {
                    // token无效，清除
                    logger.warn("Auth", "token已失效，清除保存的token");
                    storages.create("auth").remove("cardToken");
                }
            } else {
                // 使用统一错误处理
                if (!apiUtils.handleApiError(response, "token验证")) {
                    // 如果不是标准错误，清除token
                    logger.warn("Auth", "token验证失败，清除保存的token", { statusCode: response.statusCode });
                    storages.create("auth").remove("cardToken");
                }
            }
        } catch (e) {
            logger.error("Auth", "token验证出错", { error: e.message, stack: e.stack });
            storages.create("auth").remove("cardToken");
        }
    });
}

// 初始化界面
function initializeUI() {
    logger.info("UI", "初始化登录界面...");
    
    // 配置 HTTP 工具的认证参数
    httpUtils.setAuthConfig({
        clientId: config.CLIENT_ID,
        tokenStorageKey: config.STORAGE_KEYS.CARD_TOKEN,
        authStorageName: config.STORAGE_KEYS.AUTH
    });
    
    // 设置工具栏 - 现在由抽屉菜单处理
    // toolbar的navigation点击事件已在事件监听器部分设置
    
    // 从存储中加载记住的用户名和密码
    loadRememberedCredentials();
    
    // 显示设备信息
    updateDeviceInfo();
    
    // 验证现有token
    verifyExistingToken();
    
    logger.info("UI", "界面初始化完成");
}

// 加载记住的登录信息
function loadRememberedCredentials() {
    try {
        let savedCardNo = storages.create("login").get("cardNo", "");
        let savedDeviceId = storages.create("login").get("deviceId", "");
        let rememberFlag = storages.create("login").get("remember", false);
        
        if (rememberFlag && savedCardNo) {
            ui.cardNoInput.setText(savedCardNo);
            ui.deviceIdInput.setText(savedDeviceId);
            ui.rememberCard.setChecked(true);
            logger.debug("Credentials", "已加载记住的登录信息", { cardNo: savedCardNo.substring(0, 4) + "****" });
        }
    } catch (e) {
        logger.error("Credentials", "加载记住的登录信息失败", { error: e.message });
    }
}

// 保存登录信息
function saveCredentials(cardNo, deviceId, remember) {
    try {
        let storage = storages.create("login");
        if (remember) {
            storage.put("cardNo", cardNo);
            storage.put("deviceId", deviceId);
            storage.put("remember", true);
            logger.debug("Credentials", "已保存登录信息");
        } else {
            storage.remove("cardNo");
            storage.remove("deviceId");
            storage.put("remember", false);
            logger.debug("Credentials", "已清除保存的登录信息");
        }
    } catch (e) {
        logger.error("Credentials", "保存登录信息失败", { error: e.message });
    }
}

// 获取完整设备信息
function getDeviceInfo() {
    try {
        // 获取Android ID
        let androidId = "";
        try {
            androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            ) || "";
        } catch (e) {
            logger.warn("Device", "无法获取Android ID", { error: e.message });
            androidId = "unknown_" + new Date().getTime();
        }
        
        // 获取屏幕尺寸
        let windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE);
        let display = windowManager.getDefaultDisplay();
        let point = new android.graphics.Point();
        display.getSize(point);
        
        return {
            deviceAndroidId: androidId,
            deviceWidth: point.x,
            deviceHeight: point.y,
            deviceBuildId: android.os.Build.ID || "",
            deviceBroad: android.os.Build.BOARD || "",
            deviceBrand: android.os.Build.BRAND || "",
            deviceName: android.os.Build.DEVICE || "",
            deviceModel: android.os.Build.MODEL || "未知设备",
            deviceSdkInt: android.os.Build.VERSION.SDK_INT.toString(),
            deviceImei: "", // IMEI需要特殊权限，暂时留空
            deviceInfo: android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
        };
    } catch (e) {
        logger.error("Device", "获取设备信息失败", { error: e.message });
        return {
            deviceAndroidId: "unknown_" + new Date().getTime(),
            deviceWidth: 1080,
            deviceHeight: 1920,
            deviceBuildId: "",
            deviceBroad: "",
            deviceBrand: "",
            deviceName: "",
            deviceModel: device.model || "未知设备",
            deviceSdkInt: android.os.Build.VERSION.SDK_INT.toString(),
            deviceImei: "",
            deviceInfo: "Unknown Device"
        };
    }
}

// 格式化时间字符串
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return "";
    
    try {
        // 处理 "2025-09-22 23:59:59" 格式的时间字符串
        let dateStr = dateTimeStr;
        // 如果是 "YYYY-MM-DD HH:mm:ss" 格式，转换为标准格式
        if (dateStr.includes(' ') && !dateStr.includes('T')) {
            dateStr = dateStr.replace(' ', 'T');
        }
        let date = new Date(dateStr);
        if (!isNaN(date.getTime())) {
            return date.toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        } else {
            // 如果转换失败，直接返回原始字符串
            return dateTimeStr;
        }
    } catch (e) {
        logger.warn("DateTime", "日期解析失败", { error: e.message, dateTimeStr: dateTimeStr });
        return dateTimeStr;
    }
}

// 更新设备信息显示
function updateDeviceInfo() {
    try {
        let deviceInfo = getDeviceInfo();
        ui.deviceModel.setText(deviceInfo.deviceModel);
        ui.systemVersion.setText("Android " + (device.release || "Unknown"));
        
        logger.debug("Device", "设备信息已更新", {
            model: deviceInfo.deviceModel,
            systemVersion: "Android " + (device.release || "Unknown")
        });
    } catch (e) {
        logger.error("Device", "更新设备信息失败", { error: e.message });
    }
}

// 更新游戏列表
function updateGamesList(games) {
    try {
        gamesList = games || [];
        logger.info("Games", "更新可用游戏列表", { count: gamesList.length, games: gamesList.map(g => g.gameTitle) });
        
        // 准备下拉列表选项
        let gameOptions = ["请选择游戏..."];
        gamesList.forEach(game => {
            gameOptions.push(game.gameTitle);
        });
        
        ui.run(() => {
            // 更新下拉框选项
            ui.gameSpinner.attr("entries", gameOptions.join("|"));
            
            // 如果有游戏列表，默认选择第一个游戏
            if (gamesList.length > 0) {
                ui.gameSpinner.setSelection(1); // 选择第一个游戏（索引1，因为索引0是"请选择游戏..."）
                selectedGameId = gamesList[0].gameId;
                logger.debug("Games", "默认选择第一个游戏", { gameTitle: gamesList[0].gameTitle, gameId: selectedGameId });
            } else {
                selectedGameId = null;
            }
            
            // 更新按钮状态
            updateGameButtonsState();
        });
        
    } catch (e) {
        logger.error("Games", "更新游戏列表失败", { error: e.message });
    }
}

// 更新游戏按钮状态
function updateGameButtonsState() {
    let hasSelectedGame = selectedGameId !== null;
    ui.startScriptBtn.setEnabled(hasSelectedGame);
    ui.updateScriptBtn.setEnabled(hasSelectedGame);
}

// 显示状态信息
function showStatus(message, isError = false) {
    ui.run(() => {
        ui.statusText.setText(message);
        ui.statusText.setTextColor(isError ? colors.parseColor("#f44336") : colors.parseColor("#4CAF50"));
        ui.statusText.setVisibility(0); // VISIBLE
    });
}

// 隐藏状态信息
function hideStatus() {
    ui.run(() => {
        ui.statusText.setVisibility(8); // GONE
    });
}

// 显示/隐藏加载进度
function showLoading(show = true) {
    ui.run(() => {
        ui.loadingProgress.setVisibility(show ? 0 : 8); // VISIBLE : GONE
        ui.loginBtn.setEnabled(!show);
    });
}

// 登录函数
function performLogin() {
    let loginStartTime = new Date().getTime();
    let cardNo = ui.cardNoInput.text();
    let deviceId = ui.deviceIdInput.text();
    
    // 输入验证
    if (!cardNo.trim()) {
        showStatus("请输入卡密", true);
        return;
    }
    
    // 获取设备信息
    let deviceInfo = getDeviceInfo();
    
    // 如果用户没有输入设备ID，使用自动获取的
    if (!deviceId.trim()) {
        deviceId = deviceInfo.deviceAndroidId;
    }
    
    logger.info("Login", "开始登录流程", { 
        cardNo: cardNo.substring(0, 4) + "****", 
        deviceId: deviceId.substring(0, 8) + "****",
        hasCustomDeviceId: !!ui.deviceIdInput.text().trim()
    });
    showLoading(true);
    hideStatus();
    
    // 在新线程中执行登录请求
    threads.start(() => {
        try {
            // 构建登录请求数据，对应CardLoginBo
            let loginData = {
                tenantId: config.TENANT_ID, // 添加租户ID，用于多租户隔离
                cardNo: cardNo,
                deviceAndroidId: deviceId,
                deviceWidth: deviceInfo.deviceWidth,
                deviceHeight: deviceInfo.deviceHeight,
                deviceBuildId: deviceInfo.deviceBuildId,
                deviceBroad: deviceInfo.deviceBroad,
                deviceBrand: deviceInfo.deviceBrand,
                deviceName: deviceInfo.deviceName,
                deviceModel: deviceInfo.deviceModel,
                deviceSdkInt: deviceInfo.deviceSdkInt,
                deviceImei: deviceInfo.deviceImei,
                deviceInfo: deviceInfo.deviceInfo
            };
            
            logger.info("Login", "准备发送登录请求", {
                deviceModel: loginData.deviceModel,
                deviceBrand: loginData.deviceBrand,
                screenSize: loginData.deviceWidth + "x" + loginData.deviceHeight
            });
            
            // 调用真实的登录API（登录接口不需要token）
            let response = apiUtils.postWithoutAuth(API_CONFIG.ENDPOINTS.LOGIN, loginData);
            
            logger.debug("Login", "收到登录响应", { statusCode: response.statusCode });
            
            // 处理登录响应
            if (response.statusCode === 200) {
                let result = JSON.parse(response.body);
                if (result.code === 200 && result.data) {
                    // 登录成功，处理CardLoginVo数据
                    userInfo = result.data;
                    userInfo.loginTimeDisplay = new Date().toLocaleString();
                    
                    logger.info("Login", "登录成功", { 
                        cardNo: userInfo.cardNo,
                        remainingDays: userInfo.remainingDays,
                        deviceSize: userInfo.deviceSize,
                        gamesCount: userInfo.games ? userInfo.games.length : 0
                    });
                    
                    // 保存登录信息（如果选择了记住卡密）
                    let rememberFlag = ui.rememberCard.isChecked();
                    saveCredentials(cardNo, deviceId, rememberFlag);
                    
                    // 存储token用于后续请求
                    storages.create("auth").put("cardToken", userInfo.cardToken);
                    
                    // 记录登录性能
                    let loginDuration = new Date().getTime() - loginStartTime;
                    logger.logPerformance("Login", loginDuration, { 
                        success: true,
                        cardNo: userInfo.cardNo,
                        hasGames: userInfo.games && userInfo.games.length > 0
                    });
                    
                    // 启用日志上传功能
                    logger.getRawLogger().setConfig('serverUpload', true);
                    logger.info("Auth", "登录成功，已启用日志上传功能");
                    
                    // 切换到主页面
                    ui.run(() => {
                        showLoginSuccess();
                    });
                } else {
                    // 登录失败
                    ui.run(() => {
                        showLoading(false);
                        showStatus(result.msg || "登录失败", true);
                    });
                }
            } else {
                // HTTP请求失败
                ui.run(() => {
                    showLoading(false);
                    showStatus("网络请求失败，请检查网络连接", true);
                });
            }
        } catch (e) {
            logger.error("Login", "登录过程出错", { error: e.message, stack: e.stack });
            ui.run(() => {
                showLoading(false);
                showStatus("登录失败: " + e.message, true);
            });
        }
    });
}

// 显示登录成功页面
function showLoginSuccess() {
    try {
        isLoggedIn = true;
        
        // 更新卡密信息显示
        ui.displayCardNo.setText(userInfo.cardNo || "");
        ui.remainingDays.setText((userInfo.remainingDays || 0) + " 天");
        ui.deviceSize.setText((userInfo.deviceSize || "0") + " 台");
        
        // 格式化过期时间
        ui.expireTime.setText(formatDateTime(userInfo.expireTime));
        
        // 显示登录时间
        ui.loginTime.setText(userInfo.loginTimeDisplay);
        
        // 更新游戏列表
        updateGamesList(userInfo.games);
        
        // 切换界面
        ui.loginPage.setVisibility(8); // GONE
        ui.homePage.setVisibility(0);  // VISIBLE
        
        showLoading(false);
        
        logger.info("UI", "已切换到主页面");
        
        // 显示欢迎消息
        toast("卡密登录成功，剩余 " + (userInfo.remainingDays || 0) + " 天");
        
    } catch (e) {
        logger.error("UI", "显示登录成功页面失败", { error: e.message });
        showStatus("页面切换失败", true);
    }
}

// 退出登录
function performLogout() {
    try {
        logger.info("Logout", "用户退出登录", { cardNo: userInfo.cardNo || "unknown" });
        
        // 调用退出登录API
        let cardToken = storages.create("auth").get("cardToken", "");
        if (cardToken) {
            threads.start(() => {
                try {
                    apiUtils.post(API_CONFIG.ENDPOINTS.LOGOUT, {});
                } catch (e) {
                    logger.warn("Logout", "退出登录API调用失败", { error: e.message });
                }
            });
        }
        
        isLoggedIn = false;
        userInfo = {};
        gamesList = [];
        selectedGameId = null;
        
        // 清除存储的token
        storages.create("auth").remove("cardToken");
        
        // 禁用日志上传功能
        logger.getRawLogger().setConfig('serverUpload', false);
        logger.info("Auth", "用户退出登录，已禁用日志上传功能");
        
        // 清空输入框
        ui.cardNoInput.setText("");
        ui.deviceIdInput.setText("");
        ui.rememberCard.setChecked(false);
        
        // 切换界面
        ui.homePage.setVisibility(8); // GONE
        ui.loginPage.setVisibility(0); // VISIBLE
        
        hideStatus();
        
        // 重新加载记住的登录信息
        loadRememberedCredentials();
        
        toast("已退出登录");
        
    } catch (e) {
        logger.error("Logout", "退出登录失败", { error: e.message });
    }
}

// 显示退出登录确认对话框
function showLogoutDialog() {
    dialogs.confirm("确认退出", "您确定要退出登录吗？")
        .then(result => {
            if (result) {
                performLogout();
            }
        });
}

// 刷新用户信息
function refreshUserInfo() {
    try {
        logger.info("UserInfo", "开始刷新卡密信息");
        
        let cardToken = storages.create("auth").get("cardToken", "");
        if (!cardToken) {
            toast("未登录，无法刷新");
            return;
        }
        
        threads.start(() => {
            try {
                // 调用获取卡密信息API
                let response = apiUtils.get(API_CONFIG.ENDPOINTS.CARD_INFO);
                
                if (response.statusCode === 200) {
                    let result = JSON.parse(response.body);
                    if (result.code === 200 && result.data) {
                        // 更新用户信息
                        userInfo = result.data;
                        userInfo.loginTimeDisplay = new Date().toLocaleString();
                        
                        ui.run(() => {
                            // 更新显示
                            ui.displayCardNo.setText(userInfo.cardNo || "");
                            ui.remainingDays.setText((userInfo.remainingDays || 0) + " 天");
                            ui.deviceSize.setText((userInfo.deviceSize || "0") + " 台");
                            
                            ui.expireTime.setText(formatDateTime(userInfo.expireTime));
                            ui.loginTime.setText(userInfo.loginTimeDisplay);
                            
                            // 更新游戏列表
                            updateGamesList(userInfo.games);
                            
                            // 更新设备信息
                            updateDeviceInfo();
                            
                            toast("信息已刷新");
                        });
                    } else {
                        ui.run(() => {
                            toast("刷新失败: " + (result.msg || "未知错误"));
                        });
                    }
                } else {
                    // 使用统一错误处理
                    if (!apiUtils.handleApiError(response, "刷新用户信息")) {
                        ui.run(() => {
                            toast("网络请求失败");
                        });
                    }
                }
            } catch (e) {
                logger.error("UserInfo", "刷新信息失败", { error: e.message, stack: e.stack });
                ui.run(() => {
                    toast("刷新失败: " + e.message);
                });
            }
        });
        
    } catch (e) {
        logger.error("UserInfo", "刷新用户信息失败", { error: e.message, stack: e.stack });
        toast("刷新失败");
    }
}

// 更新游戏脚本
function updateGameScript(gameId) {
    if (!gameId) {
        toast("请先选择游戏");
        return;
    }
    
    let cardToken = storages.create("auth").get("cardToken", "");
    if (!cardToken) {
        toast("请先登录");
        return;
    }
    
    // 获取选中的游戏信息
    let selectedGame = gamesList.find(game => game.gameId === gameId);
    if (!selectedGame) {
        toast("无效的游戏选择");
        return;
    }
    
    logger.info("ScriptUpdate", "开始更新脚本", { gameTitle: selectedGame.gameTitle, gameId: gameId });
    
    ui.run(() => {
        toast("正在检查最新版本...");
    });
    
    threads.start(() => {
        try {
            // 调用获取最新版本API (使用不区分文件类型的统一版本接口)
            let response = apiUtils.get(API_CONFIG.ENDPOINTS.LATEST_VERSION + "/" + gameId);
            
            if (response.statusCode === 200) {
                let result = JSON.parse(response.body);
                if (result.code === 200 && result.data) {
                    let versionData = result.data;
                    logger.info("ScriptUpdate", "获取到最新版本信息", { 
                        gameTitle: selectedGame.gameTitle, 
                        version: versionData.version, 
                        type: versionData.type 
                    });
                    
                    // 检查本地是否已有脚本，如果有则比较版本
                    checkAndUpdateScript(versionData, selectedGame);
                } else {
                    ui.run(() => {
                        toast("获取版本信息失败: " + (result.msg || "未知错误"));
                    });
                }
            } else {
                // 使用统一错误处理
                if (!apiUtils.handleApiError(response, "获取最新版本")) {
                    ui.run(() => {
                        toast("检查版本失败");
                    });
                }
            }
                    } catch (e) {
                logger.error("ScriptUpdate", "更新脚本失败", { 
                    error: e.message, 
                    stack: e.stack, 
                    gameTitle: selectedGame.gameTitle 
                });
                ui.run(() => {
                    toast("更新脚本失败: " + e.message);
                });
            }
    });
}

// 检查并更新脚本
function checkAndUpdateScript(versionData, gameInfo) {
    try {
        // 构建游戏目录路径
        let scriptsDir = config.SCRIPTS_DIR;
        let gameDir = scriptsDir + gameInfo.gameTitle + "/";
        let versionInfoPath = gameDir + config.APP_CONFIG.SCRIPT_CONFIG.VERSION_FILE_NAME;
        
        // 获取文件类型描述（在函数开始时声明一次）
        let fileTypeDesc = versionData.type === 0 ? "JS文件" : "ZIP文件";
        
        // 检查是否已存在本地版本信息
        let needUpdate = true;
        let updateMessage = "发现新版本";
        
        if (files.exists(versionInfoPath)) {
            try {
                let localVersionInfo = JSON.parse(files.read(versionInfoPath));
                let localVersion = parseInt(localVersionInfo.version) || 0;
                let remoteVersion = parseInt(versionData.version) || 0;
                
                logger.info("ScriptUpdate", "版本比较", { 
                    gameTitle: gameInfo.gameTitle, 
                    localVersion: localVersion, 
                    remoteVersion: remoteVersion, 
                    fileType: fileTypeDesc 
                });
                
                if (localVersion >= remoteVersion) {
                    needUpdate = false;
                    ui.run(() => {
                        toast("已是最新版本 v" + String(remoteVersion) + " (" + fileTypeDesc + ")");
                    });
                    return;
                } else {
                    updateMessage = "发现新版本 v" + String(remoteVersion) + " (" + fileTypeDesc + ") - 当前: v" + String(localVersion);
                }
            } catch (e) {
                logger.warn("ScriptUpdate", "读取本地版本信息失败", { 
                    error: e.message, 
                    gameTitle: gameInfo.gameTitle, 
                    versionInfoPath: versionInfoPath 
                });
                updateMessage = "发现新版本 v" + String(versionData.version) + " (" + fileTypeDesc + ")";
            }
        } else {
            updateMessage = "首次下载 v" + String(versionData.version) + " (" + fileTypeDesc + ")";
        }
        
        if (needUpdate) {
            ui.run(() => {
                toast(updateMessage + "，开始更新...");
            });
            
            // 删除旧版本文件（清理所有类型的旧文件）
            cleanOldScriptFiles(gameDir, gameInfo);
            
            // 下载新版本
            downloadScriptFile(versionData, gameInfo);
        }
        
    } catch (e) {
        logger.error("ScriptUpdate", "检查脚本版本失败", { 
            error: e.message, 
            stack: e.stack, 
            gameTitle: gameInfo.gameTitle 
        });
        ui.run(() => {
            toast("检查版本失败: " + e.message);
        });
    }
}

// 清理旧的脚本文件
function cleanOldScriptFiles(gameDir, gameInfo) {
    try {
        if (!files.exists(gameDir)) {
            return; // 目录不存在，无需清理
        }
        
        logger.info("ScriptUpdate", "开始清理旧版本文件", { gameTitle: gameInfo.gameTitle, gameDir: gameDir });
        
        // 读取目录中的所有文件
        const fileList = files.listDir(gameDir);
        let deletedCount = 0;
        
        fileList.forEach(fileName => {
            const filePath = gameDir + fileName;
            
            // 跳过version.json文件，稍后会被新版本覆盖
            if (fileName === "version.json") {
                return;
            }
            
            // 删除所有脚本文件（.js和.zip文件）- 因为现在版本号统一，需要清理所有类型的旧文件
            if ((fileName.indexOf(".js") === fileName.length - 3) || (fileName.indexOf(".zip") === fileName.length - 4)) {
                try {
                    const deleted = files.remove(filePath);
                    if (deleted) {
                        logger.debug("ScriptUpdate", "已删除旧文件", { fileName: fileName, filePath: filePath });
                        deletedCount++;
                    } else {
                        logger.warn("ScriptUpdate", "删除文件失败", { fileName: fileName, filePath: filePath });
                    }
                } catch (e) {
                    logger.error("ScriptUpdate", "删除文件出错", { 
                        fileName: fileName, 
                        filePath: filePath, 
                        error: e.message 
                    });
                }
            }
        });
        
        logger.info("ScriptUpdate", "清理完成", { 
            gameTitle: gameInfo.gameTitle, 
            deletedCount: deletedCount 
        });
        
        if (deletedCount > 0) {
            ui.run(() => {
                toast("已清理 " + deletedCount + " 个旧版本文件");
            });
        }
        
    } catch (e) {
        logger.error("ScriptUpdate", "清理旧文件失败", { 
            error: e.message, 
            stack: e.stack, 
            gameTitle: gameInfo.gameTitle 
        });
        ui.run(() => {
            toast("清理旧文件失败: " + e.message);
        });
    }
}

// 下载脚本文件
function downloadScriptFile(versionData, gameInfo) {
    try {
        if (!versionData.fileUrl) {
            ui.run(() => {
                toast("脚本下载地址不存在");
            });
            return;
        }
        
        // 构建文件保存路径
        const scriptsDir = config.SCRIPTS_DIR;
        const gameDir = scriptsDir + gameInfo.gameTitle + "/";
        
        // 确保目录存在
        files.ensureDir(scriptsDir);
        files.ensureDir(gameDir);
        
        // 构建文件名 - 使用统一版本号命名
        let fileName = "script";
        const versionStr = versionData.version ? String(versionData.version) : "latest";
        if (versionData.type === 0) {
            // 单JS文件
            fileName = gameInfo.gameTitle + "_v" + versionStr + ".js";
        } else if (versionData.type === 1) {
            // ZIP文件
            fileName = gameInfo.gameTitle + "_v" + versionStr + ".zip";
        }
        
        const filePath = gameDir + fileName;
        
        logger.info("ScriptUpdate", "准备下载文件", { 
            gameTitle: gameInfo.gameTitle, 
            fileName: fileName, 
            filePath: filePath,
            fileType: versionData.type === 0 ? "JS" : "ZIP"
        });
        
        ui.run(() => {
            toast("正在下载: " + fileName);
        });
        
        // 使用HTTP工具下载文件
        threads.start(() => {
            try {
                const downloadUrl = versionData.fileUrl;
                logger.debug("ScriptUpdate", "下载地址", { 
                    downloadUrl: downloadUrl, 
                    gameTitle: gameInfo.gameTitle 
                });
                
                // 如果是相对路径，添加基础URL
                let fullUrl = downloadUrl;
                if (!downloadUrl.startsWith("http")) {
                    fullUrl = API_CONFIG.BASE_URL + downloadUrl;
                }
                
                // 使用HTTP工具下载文件 - 使用二进制模式（认证头部会自动添加）
                const response = httpUtils.get(fullUrl, { 
                    timeout: 30000, // 30秒超时
                    responseType: 'binary' // 指定二进制响应
                });
                
                if (response.statusCode === 200) {
                    // 使用HTTP工具的保存方法保存文件
                    httpUtils.saveResponseToFile(response, filePath);
                    
                    logger.info("ScriptUpdate", "文件下载成功", { 
                        gameTitle: gameInfo.gameTitle, 
                        filePath: filePath,
                        fileSize: response.body ? response.body.length : 0
                    });
                    
                    let finalFilePath = filePath;
                    let extractedFiles = [];
                    
                    // 如果是ZIP文件，进行解压缩
                    if (versionData.type === 1) {
                        ui.run(() => {
                            toast("ZIP文件下载完成，正在解压缩...");
                        });
                        
                        try {
                            const extractResult = extractZipFile(filePath, gameDir, gameInfo);
                            if (extractResult.success) {
                                extractedFiles = extractResult.extractedFiles || [];
                                logger.info("ScriptUpdate", "ZIP文件解压成功", { 
                                    gameTitle: gameInfo.gameTitle, 
                                    extractedFilesCount: extractedFiles.length,
                                    extractedFiles: extractedFiles 
                                });
                                
                                ui.run(() => {
                                    toast("解压完成，提取了 " + extractedFiles.length + " 个文件");
                                });
                                
                                // 找到主脚本文件（通常是main.js或index.js）
                                const mainScript = findMainScript(extractedFiles, gameDir);
                                if (mainScript) {
                                    finalFilePath = mainScript;
                                    logger.debug("ScriptUpdate", "找到主脚本文件", { 
                                        gameTitle: gameInfo.gameTitle, 
                                        mainScript: mainScript 
                                    });
                                }
                            } else {
                                logger.error("ScriptUpdate", "ZIP解压失败", { 
                                    gameTitle: gameInfo.gameTitle, 
                                    error: extractResult.error 
                                });
                                ui.run(() => {
                                    toast("解压失败: " + extractResult.error);
                                });
                                // 解压失败但文件已下载，继续保存版本信息
                            }
                        } catch (e) {
                            logger.error("ScriptUpdate", "解压过程出错", { 
                                gameTitle: gameInfo.gameTitle, 
                                error: e.message, 
                                stack: e.stack 
                            });
                            ui.run(() => {
                                toast("解压出错: " + e.message);
                            });
                        }
                    }
                    
                    // 保存版本信息到本地
                    const versionInfoPath = gameDir + "version.json";
                    const fileTypeDesc = versionData.type === 0 ? "JS文件" : "ZIP文件";
                    const versionInfo = {
                        gameId: gameInfo.gameId,
                        gameTitle: gameInfo.gameTitle,
                        version: String(versionData.version || "unknown"),
                        type: versionData.type,
                        typeDescription: fileTypeDesc,
                        downloadTime: new Date().toLocaleString(),
                        filePath: finalFilePath, // 对于ZIP文件，这可能是主脚本文件路径
                        originalZipPath: versionData.type === 1 ? filePath : null, // 保存原始ZIP文件路径
                        fileName: fileName,
                        extractedFiles: extractedFiles, // 记录解压出的文件列表
                        remark: versionData.remark || "",
                        versioningNote: "使用游戏统一版本号，不区分文件类型",
                        originalVersionData: versionData // 保存原始版本数据以备后用
                    };
                    files.write(versionInfoPath, JSON.stringify(versionInfo, null, 2));
                    
                    ui.run(() => {
                        let successMessage = "脚本更新成功！\n版本: v" + String(versionData.version) + " (" + fileTypeDesc + ")";
                        if (versionData.type === 1 && extractedFiles.length > 0) {
                            successMessage += "\n已解压 " + extractedFiles.length + " 个文件";
                        }
                        successMessage += "\n路径: " + finalFilePath;
                        toast(successMessage);
                    });
                } else {
                    logger.error("ScriptUpdate", "下载失败", { 
                        gameTitle: gameInfo.gameTitle, 
                        statusCode: response.statusCode,
                        fileName: fileName
                    });
                    ui.run(() => {
                        toast("下载失败，状态码: " + response.statusCode);
                    });
                }
                
            } catch (e) {
                logger.error("ScriptUpdate", "下载文件失败", { 
                    gameTitle: gameInfo.gameTitle, 
                    error: e.message, 
                    stack: e.stack,
                    fileName: fileName
                });
                ui.run(() => {
                    toast("下载失败: " + e.message);
                });
            }
        });
        
    } catch (e) {
        logger.error("ScriptUpdate", "准备下载失败", { 
            gameTitle: gameInfo.gameTitle, 
            error: e.message, 
            stack: e.stack 
        });
        ui.run(() => {
            toast("准备下载失败: " + e.message);
        });
    }
}

// 解压ZIP文件
function extractZipFile(zipFilePath, extractDir, gameInfo) {
    try {
        logger.info("ZipExtract", "开始解压ZIP文件", { 
            gameTitle: gameInfo.gameTitle, 
            zipFilePath: zipFilePath, 
            extractDir: extractDir 
        });
        
        // 确保解压目录存在
        files.ensureDir(extractDir);
        
        // 声明Java类
        var ZipFile = java.util.zip.ZipFile;
        var FileOutputStream = java.io.FileOutputStream;
        var File = java.io.File;
        var Charset = java.nio.charset.Charset;
        var BufferedInputStream = java.io.BufferedInputStream;
        var BufferedOutputStream = java.io.BufferedOutputStream;
        var byte = java.lang.Byte.TYPE;
        
        // 尝试多种编码方式解压
        var charsets = ["UTF-8", "GBK", "GB2312"];
        var success = false;
        var lastError = null;
        var extractedFiles = [];
        
        // 依次尝试不同的编码
        for (var i = 0; i < charsets.length && !success; i++) {
            try {
                logger.debug("ZipExtract", "尝试使用编码", { 
                    gameTitle: gameInfo.gameTitle, 
                    charset: charsets[i], 
                    attempt: i + 1 
                });
                
                // 创建ZipFile，指定编码
                var zipFile = null;
                try {
                    // 尝试使用指定的编码打开
                    zipFile = new ZipFile(new File(zipFilePath), Charset.forName(charsets[i]));
                } catch (e) {
                    // 如果指定编码不支持，尝试使用默认编码
                    logger.debug("ZipExtract", "不支持指定编码，使用默认编码", { 
                        gameTitle: gameInfo.gameTitle, 
                        charset: charsets[i] 
                    });
                    zipFile = new ZipFile(new File(zipFilePath));
                }
                
                var entries = zipFile.entries();
                extractedFiles = []; // 重置提取文件列表
                
                // 读取并解压每个条目
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    var entryName = entry.getName();
                    
                    // 跳过目录项、隐藏文件和系统文件
                    if (entry.isDirectory() || 
                        entryName.indexOf('.') === 0 || 
                        entryName.indexOf('__MACOSX') >= 0 ||
                        entryName.indexOf('..') >= 0) {
                        continue;
                    }
                    
                    logger.debug("ZipExtract", "解压文件", { 
                        gameTitle: gameInfo.gameTitle, 
                        entryName: entryName 
                    });
                    
                    // 构建输出文件路径，确保安全
                    var safeName = entryName.replace(/\.\./g, '').replace(/\\/g, '/');
                    var outputPath = extractDir + safeName;
                    var newFile = new File(outputPath);
                    
                    // 创建父目录
                    var parentFile = new File(newFile.getParent());
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    
                    // 创建缓冲区
                    var buffer = java.lang.reflect.Array.newInstance(byte, 4096);
                    
                    // 读取并写入文件
                    var bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    var bos = new BufferedOutputStream(new FileOutputStream(newFile));
                    
                    var bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    
                    bos.flush();
                    bos.close();
                    bis.close();
                    
                    extractedFiles.push(outputPath);
                    logger.debug("ZipExtract", "文件解压完成", { 
                        gameTitle: gameInfo.gameTitle, 
                        outputPath: outputPath 
                    });
                }
                
                zipFile.close();
                success = true;
                logger.info("ZipExtract", "成功解压文件", { 
                    gameTitle: gameInfo.gameTitle, 
                    zipFilePath: zipFilePath, 
                    extractedCount: extractedFiles.length 
                });
                
            } catch (e) {
                logger.warn("ZipExtract", "使用编码解压失败", { 
                    gameTitle: gameInfo.gameTitle, 
                    charset: charsets[i], 
                    error: e.message 
                });
                lastError = e;
                // 清空已提取的文件列表，为下一次尝试做准备
                extractedFiles = [];
            }
        }
        
        if (success) {
            return {
                success: true,
                extractedFiles: extractedFiles,
                message: "解压成功，提取了 " + extractedFiles.length + " 个文件"
            };
        } else {
            logger.error("ZipExtract", "所有编码尝试均失败", { 
                gameTitle: gameInfo.gameTitle, 
                zipFilePath: zipFilePath,
                lastError: lastError ? lastError.message : "未知错误"
            });
            return {
                success: false,
                error: lastError ? (lastError.message || "解压失败") : "所有编码尝试均失败",
                extractedFiles: []
            };
        }
        
    } catch (e) {
        logger.error("ZipExtract", "解压过程出错", { 
            gameTitle: gameInfo.gameTitle, 
            zipFilePath: zipFilePath, 
            error: e.message, 
            stack: e.stack 
        });
        return {
            success: false,
            error: e.message || "解压过程出错",
            extractedFiles: []
        };
    }
}


// 查找主脚本文件
function findMainScript(extractedFiles, gameDir) {
    try {
        // 定义可能的主脚本文件名（按优先级排序）
        const mainScriptNames = config.APP_CONFIG.SCRIPT_CONFIG.START_SCRIPT_NAMES;
        
        // 首先在根目录查找主脚本
        for (let i = 0; i < mainScriptNames.length; i++) {
            const mainName = mainScriptNames[i];
            const mainPath = gameDir + mainName;
            if (extractedFiles.indexOf(mainPath) >= 0) {
                logger.debug("ScriptFinder", "找到根目录主脚本", { mainPath: mainPath });
                return mainPath;
            }
        }
        
        // 如果根目录没有找到，查找所有JS文件
        const jsFiles = [];
        for (let i = 0; i < extractedFiles.length; i++) {
            if (extractedFiles[i].indexOf('.js') === extractedFiles[i].length - 3) {
                jsFiles.push(extractedFiles[i]);
            }
        }
        
        if (jsFiles.length === 0) {
            logger.warn("ScriptFinder", "未找到任何JS文件", { gameDir: gameDir });
            return null;
        }
        
        // 如果只有一个JS文件，就是它了
        if (jsFiles.length === 1) {
            logger.debug("ScriptFinder", "找到唯一JS文件", { jsFile: jsFiles[0] });
            return jsFiles[0];
        }
        
        // 多个JS文件时，优先选择文件名包含main、index等关键词的
        for (let i = 0; i < mainScriptNames.length; i++) {
            const mainScriptName = mainScriptNames[i];
            const keyword = mainScriptName.replace('.js', '');
            for (let j = 0; j < jsFiles.length; j++) {
                if (jsFiles[j].toLowerCase().indexOf(keyword) >= 0) {
                    logger.debug("ScriptFinder", "找到匹配的主脚本", { 
                        jsFile: jsFiles[j], 
                        keyword: keyword 
                    });
                    return jsFiles[j];
                }
            }
        }
        
        // 如果都没找到，返回第一个JS文件
        logger.debug("ScriptFinder", "使用第一个JS文件作为主脚本", { jsFile: jsFiles[0] });
        return jsFiles[0];
        
    } catch (e) {
        logger.error("ScriptFinder", "查找主脚本文件失败", { 
            error: e.message, 
            stack: e.stack, 
            gameDir: gameDir 
        });
        return null;
    }
}


// 启动游戏脚本
function startGameScript(gameId) {
    if (!gameId) {
        toast("请先选择游戏");
        return;
    }
    
    // 获取选中的游戏信息
    const selectedGame = gamesList.find(game => game.gameId === gameId);
    if (!selectedGame) {
        toast("无效的游戏选择");
        return;
    }
    
    logger.info("GameScript", "启动游戏脚本", { 
        gameTitle: selectedGame.gameTitle, 
        gameId: gameId 
    });
    
    // 先验证权限
    threads.start(() => {
        try {
            const response = apiUtils.get("/open-api/script/game-data/" + gameId);
            
            if (response.statusCode === 200) {
                const result = JSON.parse(response.body);
                if (result.code === 200) {
                    ui.run(() => {
                        toast("权限验证成功，正在启动 " + selectedGame.gameTitle + " 脚本...");
                        // 这里可以添加具体的脚本启动逻辑
                        logger.debug("GameScript", "可以启动脚本", { 
                            gameTitle: selectedGame.gameTitle, 
                            gameData: result.data 
                        });
                        
                        // 示例：启动脚本的逻辑
                        setTimeout(() => {
                            toast("脚本已启动，游戏: " + selectedGame.gameTitle);
                        }, 1000);
                    });
                } else {
                    ui.run(() => {
                        toast("无权限启动该游戏脚本: " + (result.msg || "未知错误"));
                    });
                }
            } else {
                apiUtils.handleApiError(response, "启动游戏脚本");
            }
        } catch (e) {
            logger.error("GameScript", "启动游戏脚本失败", { 
                gameTitle: selectedGame.gameTitle, 
                gameId: gameId, 
                error: e.message, 
                stack: e.stack 
            });
            ui.run(() => {
                toast("启动脚本失败: " + e.message);
            });
        }
    });
}

// 事件监听器
ui.loginBtn.click(() => {
    performLogin();
});

ui.logoutBtn.click(() => {
    showLogoutDialog();
});

ui.refreshBtn.click(() => {
    refreshUserInfo();
});

// 游戏选择下拉框事件
ui.gameSpinner.setOnItemSelectedListener({
    onItemSelected: function(parent, view, position, id) {
        if (position === 0) {
            // 选择了"请选择游戏..."
            selectedGameId = null;
        } else {
            // 选择了具体游戏
            const gameIndex = position - 1;
            if (gameIndex < gamesList.length) {
                selectedGameId = gamesList[gameIndex].gameId;
                logger.logUserAction("游戏选择", {
                    gameTitle: gamesList[gameIndex].gameTitle,
                    gameId: selectedGameId,
                    position: position
                });
            } else {
                selectedGameId = null;
            }
        }
        updateGameButtonsState();
    },
    onNothingSelected: function(parent) {
        selectedGameId = null;
        updateGameButtonsState();
    }
});

// 启动脚本按钮事件
ui.startScriptBtn.click(() => {
    startGameScript(selectedGameId);
});

// 更新脚本按钮事件
ui.updateScriptBtn.click(() => {
    updateGameScript(selectedGameId);
});

// 输入框回车事件
ui.cardNoInput.on("editor_action", (view, actionId) => {
    if (actionId === android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
        ui.deviceIdInput.requestFocus();
        return true;
    }
    return false;
});

ui.deviceIdInput.on("editor_action", (view, actionId) => {
    if (actionId === android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
        performLogin();
        return true;
    }
    return false;
});

// 抽屉菜单状态
let isDrawerOpen = false;

// 菜单按钮点击事件
ui.menuButton.click(() => {
    if (isDrawerOpen) {
        closeDrawer();
    } else {
        openDrawer();
    }
});

// 打开抽屉菜单
function openDrawer() {
    try {
        // 尝试使用标准方法
        ui.drawer.openDrawer(3); // 3 = Gravity.LEFT
    } catch (e) {
        logger.debug("UI", "无法使用openDrawer方法，尝试其他方式", { error: e.message });
        // 备用方案：使用动画效果模拟
        ui.run(() => {
            toast("抽屉菜单功能");
            // 这里可以实现一个模拟的侧边菜单
            showMenuDialog();
        });
    }
    isDrawerOpen = true;
}

// 关闭抽屉菜单
function closeDrawer() {
    try {
        ui.drawer.closeDrawer(3); // 3 = Gravity.LEFT
    } catch (e) {
        logger.debug("UI", "无法使用closeDrawer方法", { error: e.message });
    }
    isDrawerOpen = false;
}

// 显示菜单对话框作为备用方案
function showMenuDialog() {
    let accessibilityText = "♿ 无障碍服务：";
    try {
        accessibilityText += (auto.service != null) ? "开启" : "关闭";
    } catch (e) {
        accessibilityText += "未知";
    }
    
    const menuOptions = [
        accessibilityText,
        "ℹ️ 关于",
        "🚪 退出"
    ];
    
    dialogs.select("菜单", menuOptions).then(index => {
        if (index >= 0) {
            if (index === 0) {
                // 切换无障碍服务
                try {
                    const currentStatus = auto.service != null;
                    toggleAccessibilityService(!currentStatus);
                } catch (e) {
                    toast("操作失败：" + e.message);
                }
            } else {
                const actions = ["", "about", "logout"];
                handleMenuItemClick(actions[index]);
            }
        }
    });
}

// 抽屉菜单按钮事件已在initMenuButtons函数中设置

// 处理菜单项点击
function handleMenuItemClick(action) {
    switch (action) {
        case "about":
            ui.run(() => {
                showAboutDialog();
            });
            break;
            
        case "logout":
            if (isLoggedIn) {
                showLogoutDialog();
            } else {
                ui.run(() => {
                    toast("您尚未登录");
                });
            }
            break;
    }
}

// 切换无障碍服务状态
function toggleAccessibilityService(isChecked) {
    try {
        const currentStatus = auto.service != null;
        
        if (isChecked && !currentStatus) {
            // 用户想要开启无障碍服务
            try {
                auto();
                toast("正在跳转到无障碍设置页面，请开启AutoJS的无障碍服务");
                closeDrawer();
                // 延迟检查状态
                setTimeout(() => {
                    updateAccessibilitySwitch();
                }, 3000);
            } catch (e) {
                toast("无法自动跳转，请手动在设置中开启无障碍服务");
                ui.accessibilitySwitch.setChecked(false);
            }
        } else if (!isChecked && currentStatus) {
            // 用户想要关闭无障碍服务
            try {
                app.startActivity({
                    action: "android.settings.ACCESSIBILITY_SETTINGS"
                });
                toast("请在设置中关闭AutoJS的无障碍服务");
                closeDrawer();
                // 延迟检查状态
                setTimeout(() => {
                    updateAccessibilitySwitch();
                }, 3000);
            } catch (e) {
                toast("无法打开无障碍设置页面");
                ui.accessibilitySwitch.setChecked(true);
            }
        }
    } catch (e) {
        toast("操作失败：" + e.message);
        // 恢复到实际状态
        updateAccessibilitySwitch();
    }
}

// 更新无障碍开关的显示状态
function updateAccessibilitySwitch() {
    try {
        ui.run(() => {
            const isEnabled = auto.service != null;
            ui.accessibilitySwitch.setChecked(isEnabled);
            logger.debug("Accessibility", "无障碍服务状态", { isEnabled: isEnabled });
        });
    } catch (e) {
        logger.warn("Accessibility", "更新无障碍开关状态失败", { error: e.message });
    }
}

// 浮动窗口相关变量
let floatyWindow = null;
let isFloatyEnabled = false;
let isFloatyExpanded = false;
let floatyButtons = [];

// 切换浮动窗口服务状态
function toggleFloatyService(isChecked) {
    try {
        if (isChecked) {
            // 用户想要开启浮动窗口
            try {
                createFloatyWindow();
                closeDrawer();
            } catch (e) {
                // 如果创建失败，可能是权限问题
                logger.warn("Floaty", "创建悬浮窗失败", { error: e.message });
                toast("创建悬浮图标失败，可能需要悬浮窗权限");
                try {
                    // 尝试跳转到悬浮窗权限设置页面
                    app.startActivity({
                        action: "android.settings.action.MANAGE_OVERLAY_PERMISSION",
                        data: "package:" + context.getPackageName()
                    });
                } catch (e2) {
                    try {
                        // 备用方案：跳转到应用权限设置
                        app.startActivity({
                            action: "android.settings.APPLICATION_DETAILS_SETTINGS",
                            data: "package:" + context.getPackageName()
                        });
                    } catch (e3) {
                        toast("请手动在设置中开启悬浮窗权限");
                    }
                }
                ui.floatySwitch.setChecked(false);
                closeDrawer();
            }
        } else {
            // 用户想要关闭浮动窗口
            removeFloatyWindow();
            closeDrawer();
        }
    } catch (e) {
        toast("操作失败：" + e.message);
        updateFloatySwitch();
    }
}

// 添加拖动功能到悬浮窗
function addDragFunctionality(window) {
    try {
        const button = window.mainButton;
        let startX = 0, startY = 0;
        let windowX = 0, windowY = 0;
        let isDragging = false;
        let startTime = 0;
        
        // 监听触摸事件
        button.setOnTouchListener(function(view, event) {
            try {
                switch(event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // 记录开始位置和时间
                        startX = event.getRawX();
                        startY = event.getRawY();
                        windowX = window.getX();
                        windowY = window.getY();
                        isDragging = false;
                        startTime = Date.now();
                        return true;
                        
                    case android.view.MotionEvent.ACTION_MOVE:
                        // 计算移动距离
                        const deltaX = event.getRawX() - startX;
                        const deltaY = event.getRawY() - startY;
                        const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        
                        // 如果移动距离超过阈值，开始拖动
                        if (distance > 10 && !isDragging) {
                            isDragging = true;
                            // 拖动开始时，先隐藏展开的悬浮菜单
                            if (isFloatyExpanded) {
                                collapseFloatyMenu();
                            }
                        }
                        
                        if (isDragging) {
                            // 更新悬浮窗位置
                            const newX = windowX + deltaX;
                            const newY = windowY + deltaY;
                            
                            // 获取屏幕尺寸限制位置
                            const screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                            const screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                            const buttonWidth = button.getWidth();
                            const buttonHeight = button.getHeight();
                            
                            // 限制在屏幕范围内
                            const clampedX = Math.max(0, Math.min(newX, screenWidth - buttonWidth));
                            const clampedY = Math.max(0, Math.min(newY, screenHeight - buttonHeight));
                            
                            window.setPosition(clampedX, clampedY);
                        }
                        return true;
                        
                    case android.view.MotionEvent.ACTION_UP:
                        const endTime = Date.now();
                        const touchDuration = endTime - startTime;
                        
                        // 如果没有拖动且触摸时间较短，触发点击事件
                        if (!isDragging && touchDuration < 300) {
                            // 延迟一点执行点击，避免与拖动冲突
                            setTimeout(() => {
                                toggleFloatyMenu();
                            }, 50);
                        } else if (isDragging) {
                            // 拖动结束，可以添加吸边效果
                            snapToEdge(window);
                        }
                        
                        isDragging = false;
                        return true;
                }
            } catch (e) {
                logger.error("Floaty", "拖动事件处理失败", { error: e.message });
            }
            return false;
        });
        
        logger.debug("Floaty", "拖动功能添加成功");
    } catch (e) {
        logger.error("Floaty", "添加拖动功能失败", { error: e.message, stack: e.stack });
    }
}

// 悬浮窗吸边效果
function snapToEdge(window) {
    try {
        const screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        const currentX = window.getX();
        const currentY = window.getY();
        const buttonWidth = 48 * context.getResources().getDisplayMetrics().density; // 48dp转px
        
        // 判断吸向左边还是右边
        const centerX = currentX + buttonWidth / 2;
        let targetX;
        
        if (centerX < screenWidth / 2) {
            // 吸向左边
            targetX = 0;
        } else {
            // 吸向右边
            targetX = screenWidth - buttonWidth;
        }
        
        // 平滑移动到边缘
        const animator = android.animation.ValueAnimator.ofFloat(currentX, targetX);
        animator.setDuration(200);
        animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener({
            onAnimationUpdate: function(animation) {
                try {
                    const animatedValue = animation.getAnimatedValue();
                    window.setPosition(animatedValue, currentY);
                } catch (e) {
                    logger.error("Floaty", "动画更新失败", { error: e.message });
                }
            }
        }));
        animator.start();
        
        logger.debug("Floaty", "悬浮窗吸边完成");
    } catch (e) {
        logger.error("Floaty", "悬浮窗吸边失败", { error: e.message, stack: e.stack });
    }
}

// 创建浮动窗口
function createFloatyWindow() {
    try {
        if (floatyWindow) {
            removeFloatyWindow();
        }
        
        // 创建主悬浮按钮 - 简单方法，稍后编程设置圆形
        floatyWindow = floaty.window(
            <button text="⚡" textSize="18sp" textColor="#ffffff" 
                    bg="#4CAF50" w="48dp" h="48dp" 
                    id="mainButton"/>
        );
        
        // 延迟设置位置和事件
        setTimeout(() => {
            try {
                if (floatyWindow) {
                    floatyWindow.setPosition(50, 200);
                    
                    // 尝试设置圆形背景
                    try {
                        const button = floatyWindow.mainButton;
                        // 创建圆形drawable
                        const drawable = new android.graphics.drawable.GradientDrawable();
                        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                        drawable.setColor(android.graphics.Color.parseColor("#4CAF50"));
                        button.setBackground(drawable);
                        logger.debug("Floaty", "成功设置圆形背景");
                    } catch (e) {
                        logger.debug("Floaty", "设置圆形背景失败", { error: e.message });
                    }
                    
                    // 添加拖动功能（包含点击处理）
                    addDragFunctionality(floatyWindow);
                }
            } catch (e) {
                logger.debug("Floaty", "设置悬浮窗属性失败", { error: e.message });
            }
        }, 200);
        
        isFloatyEnabled = true;
        isFloatyExpanded = false;
        updateFloatySwitch();
        toast("悬浮图标已显示");
        
    } catch (e) {
        logger.error("Floaty", "创建悬浮窗失败", { error: e.message, stack: e.stack });
        toast("创建悬浮图标失败，可能需要悬浮窗权限");
        isFloatyEnabled = false;
        updateFloatySwitch();
        throw e;
    }
}

// 切换悬浮菜单的展开/收起
function toggleFloatyMenu() {
    if (isFloatyExpanded) {
        collapseFloatyMenu();
    } else {
        expandFloatyMenu();
    }
}

// 展开悬浮菜单
function expandFloatyMenu() {
    try {
        // 清除现有的按钮
        collapseFloatyMenu();
        
        // 获取主按钮位置
        const mainX = floatyWindow.getX();
        const mainY = floatyWindow.getY();
        
        // 创建四个功能按钮 - 180度半圆环绕分布
        // 简化脚本运行状态检查
        const isScriptRunning = currentRunningScript && currentRunningScript.engine;
        
        const buttons = [
            { 
                text: isScriptRunning ? "⏹️" : "▶️", 
                color: isScriptRunning ? "#f44336" : "#FF9800", 
                action: "toggle" 
            },   // 启动/停止
            { text: "📋", color: "#2196F3", action: "log" },      // 日志
            { text: "🏠", color: "#9C27B0", action: "home" },     // 首页
            { text: "❌", color: "#666666", action: "close" }     // 关闭
        ];
        
        // 180度半圆分布参数
        const radius = 120; // 半径距离
        const startAngle = -90; // 起始角度：从上方开始
        const totalAngle = 180; // 总角度：180度半圆
        const angleStep = totalAngle / (buttons.length - 1); // 每个按钮间隔60度
        
        // 创建每个按钮
        buttons.forEach((btn, index) => {
            setTimeout(() => {
                try {
                    // 计算按钮位置（180度半圆分布）
                    const angle = startAngle + index * angleStep; // 计算当前按钮角度
                    const angleRad = angle * Math.PI / 180; // 转换为弧度
                    const offsetX = Math.cos(angleRad) * radius;
                    const offsetY = Math.sin(angleRad) * radius;
                    
                    const button = floaty.window(
                        <button text={btn.text} textSize="14sp" textColor="#ffffff" 
                                bg={btn.color} w="36dp" h="36dp" 
                                id="actionBtn"/>
                    );
                    
                    button.setPosition(mainX + offsetX, mainY + offsetY);
                    
                    // 尝试设置圆形背景
                    setTimeout(() => {
                        try {
                            const actionButton = button.actionBtn;
                            const drawable = new android.graphics.drawable.GradientDrawable();
                            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                            drawable.setColor(android.graphics.Color.parseColor(btn.color));
                            actionButton.setBackground(drawable);
                        } catch (e) {
                            logger.debug("Floaty", "设置功能按钮圆形背景失败", { error: e.message });
                        }
                    }, 50);
                    
                    // 添加点击事件
                    button.actionBtn.click(() => {
                        handleFloatyAction(btn.action);
                    });
                    
                    floatyButtons.push(button);
                } catch (e) {
                    logger.warn("Floaty", "创建悬浮按钮失败", { error: e.message, buttonText: btn.text });
                }
            }, index * 100); // 增加间隔时间，让动画更明显
        });
        
        isFloatyExpanded = true;
        
    } catch (e) {
        logger.error("Floaty", "展开悬浮菜单失败", { error: e.message, stack: e.stack });
    }
}

// 收起悬浮菜单
function collapseFloatyMenu() {
    try {
        floatyButtons.forEach(button => {
            if (button) {
                button.close();
            }
        });
        floatyButtons = [];
        isFloatyExpanded = false;
    } catch (e) {
        logger.warn("Floaty", "收起悬浮菜单失败", { error: e.message });
    }
}

// 处理悬浮按钮动作
function handleFloatyAction(action) {
    collapseFloatyMenu(); // 先收起菜单
    
    switch (action) {
        case "toggle":
            // 启动/停止脚本
            const isScriptRunning = currentRunningScript && currentRunningScript.engine;
            
            if (isScriptRunning) {
                // 停止当前运行的脚本
                try {
                    // 尝试不同的停止方法
                    if (currentRunningScript.engine.getEngine().forceStop) {
                        currentRunningScript.engine.getEngine().forceStop();
                    } else if (currentRunningScript.engine.getEngine().destroy) {
                        currentRunningScript.engine.getEngine().destroy();
                    } else {
                        // 如果没有停止方法，只清除记录
                        logger.debug("ScriptEngine", "无法直接停止脚本，清除运行记录");
                    }
                    toast("脚本已停止");
                    currentRunningScript = null;
                } catch (e) {
                    logger.error("ScriptEngine", "停止脚本失败", { error: e.message, stack: e.stack });
                    toast("停止脚本失败，已清除运行记录");
                    currentRunningScript = null;
                }
            } else {
                // 启动脚本 - 检查版本后启动
                if (isLoggedIn && selectedGameId) {
                    checkVersionAndStartScript(selectedGameId);
                } else {
                    toast("请先登录并选择游戏");
                }
            }
            break;
            
        case "log":
            // 显示日志信息
            showLogDialog();
            break;
            
        case "home":
            // 返回首页 - 打开当前应用
            try {
                // 方法1: 尝试通过包名启动当前应用
                app.startActivity({
                    packageName: context.getPackageName(),
                    className: context.getPackageName() + ".MainActivity"
                });
                toast("正在打开应用");
            } catch (e) {
                try {
                    // 方法2: 使用Intent启动主Activity
                    const intent = new android.content.Intent();
                    intent.setAction(android.content.Intent.ACTION_MAIN);
                    intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
                    intent.setPackage(context.getPackageName());
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                    toast("正在打开应用");
                } catch (e2) {
                    try {
                        // 方法3: 使用App模块启动
                        app.launch(context.getPackageName());
                        toast("正在打开应用");
                    } catch (e3) {
                        logger.warn("Floaty", "打开应用失败", { error: e3.message });
                        toast("无法打开应用");
                    }
                }
            }
            break;
            
        case "close":
            // 关闭悬浮窗
            removeFloatyWindow();
            ui.floatySwitch.setChecked(false);
            break;
    }
}

// 检查版本并启动脚本
function checkVersionAndStartScript(gameId) {
    if (!gameId) {
        toast("请先选择游戏");
        return;
    }
    
    const cardToken = storages.create("auth").get("cardToken", "");
    if (!cardToken) {
        toast("请先登录");
        return;
    }
    
    // 获取选中的游戏信息
    const selectedGame = gamesList.find(game => game.gameId === gameId);
    if (!selectedGame) {
        toast("无效的游戏选择");
        return;
    }
    
    logger.info("ScriptExecution", "开始检查脚本版本", { 
        gameTitle: selectedGame.gameTitle, 
        gameId: gameId 
    });
    toast("正在检查脚本版本...");
    
    threads.start(() => {
        try {
            // 先检查本地版本
            const localVersionInfo = getLocalVersionInfo(selectedGame);
            if (!localVersionInfo) {
                ui.run(() => {
                    toast("未找到本地脚本，请先更新脚本");
                });
                return;
            }
            
            // 获取服务器最新版本
            const response = apiUtils.get(API_CONFIG.ENDPOINTS.LATEST_VERSION + "/" + gameId);
            
            if (response.statusCode === 200) {
                const result = JSON.parse(response.body);
                if (result.code === 200 && result.data) {
                    const serverVersionData = result.data;
                    const localVersion = parseInt(localVersionInfo.version) || 0;
                    const serverVersion = parseInt(serverVersionData.version) || 0;
                    
                    logger.info("ScriptExecution", "版本对比", { 
                        gameTitle: selectedGame.gameTitle, 
                        localVersion: localVersion, 
                        serverVersion: serverVersion 
                    });
                    
                    if (localVersion < serverVersion) {
                        // 本地版本过旧
                        ui.run(() => {
                            const fileTypeDesc = serverVersionData.type === 0 ? "JS文件" : "ZIP文件";
                            toast(`脚本版本过旧！\n本地: v${localVersion}\n最新: v${serverVersion} (${fileTypeDesc})\n请先更新脚本`);
                        });
                        return;
                    } else if (localVersion === serverVersion) {
                        // 版本一致，可以启动
                        ui.run(() => {
                            toast("版本检查通过，正在启动脚本...");
                        });
                        startLocalScript(selectedGame, localVersionInfo);
                    } else {
                        // 本地版本比服务器新（开发版本）
                        ui.run(() => {
                            toast(`检测到开发版本 v${localVersion}，正在启动...`);
                        });
                        startLocalScript(selectedGame, localVersionInfo);
                    }
                } else {
                    ui.run(() => {
                        toast("获取服务器版本失败: " + (result.msg || "未知错误"));
                    });
                }
            } else {
                // 使用统一错误处理
                if (!apiUtils.handleApiError(response, "检查脚本版本")) {
                    ui.run(() => {
                        toast("检查版本失败，将尝试启动本地脚本");
                    });
                    // 网络错误时仍然尝试启动本地脚本
                    startLocalScript(selectedGame, localVersionInfo);
                }
            }
        } catch (e) {
            logger.error("ScriptExecution", "检查版本失败", { 
                gameTitle: selectedGame.gameTitle, 
                gameId: gameId, 
                error: e.message, 
                stack: e.stack 
            });
            ui.run(() => {
                toast("版本检查失败: " + e.message);
            });
        }
    });
}

// 获取本地版本信息
function getLocalVersionInfo(gameInfo) {
    try {
        const scriptsDir = config.SCRIPTS_DIR;
        const gameDir = scriptsDir + gameInfo.gameTitle + "/";
        const versionInfoPath = gameDir + config.APP_CONFIG.SCRIPT_CONFIG.VERSION_FILE_NAME;
        
        if (!files.exists(versionInfoPath)) {
            logger.debug("ScriptExecution", "未找到本地版本信息文件", { 
                gameTitle: gameInfo.gameTitle, 
                versionInfoPath: versionInfoPath 
            });
            return null;
        }
        
        const versionInfoContent = files.read(versionInfoPath);
        const versionInfo = JSON.parse(versionInfoContent);
        
        logger.debug("ScriptExecution", "读取到本地版本信息", { 
            gameTitle: gameInfo.gameTitle, 
            version: versionInfo.version, 
            type: versionInfo.type 
        });
        return versionInfo;
        
    } catch (e) {
        logger.error("ScriptExecution", "读取本地版本信息失败", { 
            gameTitle: gameInfo.gameTitle, 
            error: e.message, 
            stack: e.stack 
        });
        return null;
    }
}

// 启动本地脚本
function startLocalScript(gameInfo, versionInfo) {
    try {
        const scriptsDir = config.SCRIPTS_DIR;
        const gameDir = scriptsDir + gameInfo.gameTitle + "/";
        
        // 查找start.js文件
        const startScriptPath = findStartScript(gameDir, versionInfo);
        
        if (!startScriptPath) {
            ui.run(() => {
                toast("未找到start.js启动文件");
            });
            return;
        }
        
        logger.info("ScriptExecution", "准备启动脚本", { 
            gameTitle: gameInfo.gameTitle, 
            startScriptPath: startScriptPath 
        });
        
        ui.run(() => {
            toast(`正在启动 ${gameInfo.gameTitle} 脚本...`);
        });
        
        // 验证游戏权限后启动脚本
        verifyGamePermissionAndStart(gameInfo.gameId, startScriptPath, gameDir);
        
    } catch (e) {
        logger.error("ScriptExecution", "启动本地脚本失败", { 
            gameTitle: gameInfo.gameTitle, 
            error: e.message, 
            stack: e.stack 
        });
        ui.run(() => {
            toast("启动脚本失败: " + e.message);
        });
    }
}

// 查找start.js文件
function findStartScript(gameDir, versionInfo) {
    try {
        // 定义可能的启动脚本文件名（按优先级排序）
        const startScriptNames = config.APP_CONFIG.SCRIPT_CONFIG.START_SCRIPT_NAMES;
        
        // 首先在根目录查找启动脚本
        for (let i = 0; i < startScriptNames.length; i++) {
            const scriptName = startScriptNames[i];
            const scriptPath = gameDir + scriptName;
            if (files.exists(scriptPath)) {
                logger.debug("ScriptFinder", "找到启动脚本", { scriptPath: scriptPath });
                return scriptPath;
            }
        }
        
        // 如果版本信息中有解压文件列表，在其中查找
        if (versionInfo && versionInfo.extractedFiles && versionInfo.extractedFiles.length > 0) {
            for (let i = 0; i < startScriptNames.length; i++) {
                const targetScriptName = startScriptNames[i];
                for (let j = 0; j < versionInfo.extractedFiles.length; j++) {
                    const extractedFile = versionInfo.extractedFiles[j];
                    if (extractedFile.endsWith(targetScriptName)) {
                        logger.debug("ScriptFinder", "在解压文件中找到启动脚本", { 
                            extractedFile: extractedFile, 
                            targetScriptName: targetScriptName 
                        });
                        return extractedFile;
                    }
                }
            }
        }
        
        // 如果都没找到，返回版本信息中记录的主文件路径
        if (versionInfo && versionInfo.filePath) {
            const mainFilePath = versionInfo.filePath;
            if (files.exists(mainFilePath) && mainFilePath.endsWith('.js')) {
                logger.debug("ScriptFinder", "使用版本信息中的主文件", { mainFilePath: mainFilePath });
                return mainFilePath;
            }
        }
        
        logger.warn("ScriptFinder", "未找到任何启动脚本文件", { gameDir: gameDir });
        return null;
        
    } catch (e) {
        logger.error("ScriptFinder", "查找启动脚本文件失败", { 
            error: e.message, 
            stack: e.stack, 
            gameDir: gameDir 
        });
        return null;
    }
}

// 验证游戏权限并启动脚本
function verifyGamePermissionAndStart(gameId, scriptPath, baseDir) {
    try {
        const response = apiUtils.get("/open-api/script/game-data/" + gameId);
        
        if (response.statusCode === 200) {
            const result = JSON.parse(response.body);
            if (result.code === 200) {
                logger.info("ScriptExecution", "权限验证成功", { 
                    gameId: gameId, 
                    gameDataSize: result.data ? Object.keys(result.data).length : 0 
                });
                
                // 权限验证成功，启动脚本
                ui.run(() => {
                    toast("权限验证成功，正在启动脚本...");
                });
                
                // 延迟一点启动，让toast显示
                setTimeout(() => {
                    executeScript(scriptPath, baseDir);
                }, 1000);
                
            } else {
                ui.run(() => {
                    toast("无权限启动该游戏脚本: " + (result.msg || "未知错误"));
                });
            }
        } else {
            apiUtils.handleApiError(response, "验证游戏权限");
        }
    } catch (e) {
        logger.error("ScriptExecution", "验证游戏权限失败", { 
            gameId: gameId, 
            error: e.message, 
            stack: e.stack 
        });
        ui.run(() => {
            toast("权限验证失败: " + e.message);
        });
    }
}

// 执行脚本文件
function executeScript(scriptPath, baseDir) {
    try {
        logger.info("ScriptExecution", "开始执行脚本", { 
            scriptPath: scriptPath, 
            baseDir: baseDir 
        });
        
        if (!files.exists(scriptPath)) {
            ui.run(() => {
                toast("脚本文件不存在: " + scriptPath);
            });
            return;
        }
        
        // 读取脚本内容验证
        const scriptContent = files.read(scriptPath);
        if (!scriptContent || scriptContent.trim().length === 0) {
            ui.run(() => {
                toast("脚本文件内容为空或无法读取");
            });
            return;
        }
        
        // 如果有脚本正在运行，询问是否停止
        if (currentRunningScript && currentRunningScript.engine) {
            ui.run(() => {
                dialogs.confirm("脚本运行中", "检测到有脚本正在运行，是否停止当前脚本并启动新脚本？")
                    .then(result => {
                        if (result) {
                            // 停止当前脚本
                            try {
                                if (currentRunningScript.engine.getEngine().forceStop) {
                                    currentRunningScript.engine.getEngine().forceStop();
                                } else if (currentRunningScript.engine.getEngine().destroy) {
                                    currentRunningScript.engine.getEngine().destroy();
                                }
                                toast("已停止当前脚本");
                                currentRunningScript = null;
                                // 延迟启动新脚本
                                setTimeout(() => {
                                    startScriptEngine(scriptPath, baseDir);
                                }, 1000);
                            } catch (e) {
                                logger.error("ScriptExecution", "停止脚本失败", { 
                                    error: e.message, 
                                    stack: e.stack 
                                });
                                toast("停止当前脚本失败，将清除运行记录");
                                currentRunningScript = null;
                                // 仍然启动新脚本
                                setTimeout(() => {
                                    startScriptEngine(scriptPath, baseDir);
                                }, 1000);
                            }
                        } else {
                            toast("已取消启动新脚本");
                        }
                    });
            });
            return;
        }
        
        // 启动新脚本
        startScriptEngine(scriptPath, baseDir);
        
    } catch (e) {
        logger.error("ScriptExecution", "执行脚本失败", { 
            scriptPath: scriptPath, 
            baseDir: baseDir, 
            error: e.message, 
            stack: e.stack 
        });
        ui.run(() => {
            toast("执行脚本失败: " + e.message);
        });
    }
}

// 启动脚本引擎
function startScriptEngine(scriptPath, baseDir) {
    try {
        ui.run(() => {
            toast("正在启动脚本...");
        });
        
        // 使用engines模块在新线程中执行脚本
        const engine = engines.execScriptFile(scriptPath, {
            workingDirectory: baseDir
        });
        
        // 保存当前运行的脚本引擎
        currentRunningScript = {
            engine: engine,
            scriptPath: scriptPath,
            baseDir: baseDir,
            startTime: new Date(),
            getEngine: function() { return this.engine; },
            isRunning: function() {
                try {
                    // 检查引擎是否还在运行 - 使用更简单的方法
                    return this.engine && this.engine.toString().indexOf("Execution") >= 0;
                } catch (e) {
                    return false;
                }
            }
        };
        
        // AutoJS6 不支持 engine.on 事件监听，使用轮询方式检查脚本状态
        logger.info("ScriptEngine", "脚本启动成功", { 
            scriptPath: scriptPath, 
            baseDir: baseDir, 
            engineInfo: engine.toString() 
        });
        
        ui.run(() => {
            toast("脚本已启动！");
        });
        
        // 简化的状态管理 - 不使用复杂的监控
        logger.debug("ScriptEngine", "脚本引擎对象", { 
            engineString: engine.toString() 
        });
        logger.warn("ScriptEngine", "注意：脚本状态监控功能在 AutoJS6 中有限制，请手动检查脚本运行状态");
        
    } catch (e) {
        logger.error("ScriptEngine", "启动脚本引擎失败", { 
            scriptPath: scriptPath, 
            baseDir: baseDir, 
            error: e.message, 
            stack: e.stack 
        });
        ui.run(() => {
            toast("启动脚本失败: " + e.message);
        });
        currentRunningScript = null;
    }
}

// 显示日志对话框
function showLogDialog() {
    try {
        // 收集当前状态信息
        const currentTime = new Date().toLocaleString();
        let accessibilityStatus = "未知";
        try {
            accessibilityStatus = auto.service != null ? "已开启" : "未开启";
        } catch (e) {
            accessibilityStatus = "检测失败";
        }
        
                 // 获取用户信息
         const userStatus = isLoggedIn ? 
             `已登录 - ${userInfo.cardNo || "未知卡密"}` : 
             "未登录";
            
         const gameStatus = selectedGameId ? 
             `已选择 - ${gamesList.find(g => g.gameId === selectedGameId)?.gameTitle || "未知游戏"}` :
             "未选择";
             
         // 获取脚本运行状态
         let scriptStatus = "未运行";
         if (currentRunningScript && currentRunningScript.engine) {
             try {
                 const duration = new Date() - currentRunningScript.startTime;
                 const durationStr = Math.floor(duration / 1000) + "秒";
                 scriptStatus = `运行中 (${durationStr})`;
             } catch (e) {
                 scriptStatus = "状态未知";
             }
         }
            
        // 构建状态日志内容
        const statusLog = `
╔══════════════════════════════════════╗
║            系统状态日志              ║
╠══════════════════════════════════════╣
║ 📊 当前时间: ${currentTime}
║ 
║ 👤 登录状态: ${userStatus}
║ 🎮 选中游戏: ${gameStatus}
║ ⚡ 脚本状态: ${scriptStatus}
║ ♿ 无障碍服务: ${accessibilityStatus}
║ 🌐 悬浮窗状态: ${isFloatyEnabled ? "已开启" : "未开启"}
║ 📱 设备型号: ${device.model || "未知"}
║ 🤖 系统版本: Android ${device.release || "未知"}
║
╠══════════════════════════════════════╣
║ 🔧 最近操作记录:
║ • 应用启动时间: ${currentTime}
║ • 网络状态: ${isLoggedIn ? "已连接API" : "未连接"}
║ • 权限状态: ${accessibilityStatus}
╚══════════════════════════════════════╝
        `.trim();
        
        // 输出状态日志到控制台显示
        console.log(statusLog);
        
        // 同时记录到日志系统
        logger.info("SystemStatus", "系统状态日志", {
            loginStatus: isLoggedIn,
            gameStatus: selectedGameId ? "已选择" : "未选择",
            scriptStatus: currentRunningScript ? "运行中" : "未运行",
            accessibilityStatus: accessibilityStatus,
            floatyStatus: isFloatyEnabled
        });
        
        // 如果已登录，输出用户详细信息
        if (isLoggedIn && userInfo) {
            console.log("\n" + "=".repeat(50));
            console.log("📋 用户详细信息:");
            console.log("=".repeat(50));
            console.log(`卡密: ${userInfo.cardNo || "未知"}`);
            console.log(`剩余天数: ${userInfo.remainingDays || 0} 天`);
            console.log(`过期时间: ${formatDateTime(userInfo.expireTime) || "未知"}`);
            console.log(`可绑定设备: ${userInfo.deviceSize || 0} 台`);
            console.log(`登录时间: ${userInfo.loginTimeDisplay || "未知"}`);
            
            // 记录用户信息到日志
            logger.info("UserInfo", "用户详细信息", {
                cardNo: userInfo.cardNo,
                remainingDays: userInfo.remainingDays,
                expireTime: userInfo.expireTime,
                deviceSize: userInfo.deviceSize,
                loginTime: userInfo.loginTimeDisplay
            });
            
            if (gamesList && gamesList.length > 0) {
                console.log("\n" + "=".repeat(50));
                console.log("🎮 可用游戏列表:");
                console.log("=".repeat(50));
                gamesList.forEach((game, index) => {
                    const isSelected = game.gameId === selectedGameId ? "✅" : "⭕";
                    console.log(`${isSelected} ${index + 1}. ${game.gameTitle} (ID: ${game.gameId})`);
                });
                
                // 记录游戏列表到日志
                logger.info("Games", "可用游戏列表", {
                    gamesCount: gamesList.length,
                    selectedGameId: selectedGameId,
                    games: gamesList.map(g => ({ gameId: g.gameId, gameTitle: g.gameTitle }))
                });
            }
        }
        
        // 配置并显示控制台
        console
        .setSize(0.9, 0.7)           // 增大显示区域
        .setPosition(0.05, 0.1)      // 调整位置更居中
        .setTitle('📊 ' + config.APP_NAME + ' - 系统日志')
        .setTitleTextSize(16)
        .setContentTextSize(12)      // 稍微减小内容字体以显示更多信息
        .setBackgroundColor('#263238')     // 使用深色背景，更适合日志显示
        .setTitleBackgroundAlpha(0.95)     // 增加标题背景透明度
        .setContentBackgroundAlpha(0.9)    // 增加内容背景透明度
        .setExitOnClose(10000)       // 10秒后自动关闭
        .show();
        
    } catch (e) {
        logger.error("UI", "显示日志对话框失败", { error: e.message, stack: e.stack });
        // 备用方案：简单的toast提示
        toast("无法显示日志信息");
    }
}

// 移除浮动窗口
function removeFloatyWindow() {
    try {
        // 先收起所有扩展按钮
        collapseFloatyMenu();
        
        // 关闭主悬浮窗
        if (floatyWindow) {
            floatyWindow.close();
            floatyWindow = null;
        }
        
        isFloatyEnabled = false;
        isFloatyExpanded = false;
        updateFloatySwitch();
        toast("悬浮图标已关闭");
    } catch (e) {
        logger.error("Floaty", "关闭悬浮窗失败", { error: e.message, stack: e.stack });
    }
}

// 旧的showFloatyMenu函数已被扇形展开菜单替代

// 更新浮动窗口开关的显示状态
function updateFloatySwitch() {
    try {
        ui.run(() => {
            ui.floatySwitch.setChecked(isFloatyEnabled);
            logger.debug("Floaty", "浮动窗口状态", { isEnabled: isFloatyEnabled });
        });
    } catch (e) {
        logger.warn("Floaty", "更新浮动窗口开关状态失败", { error: e.message });
    }
}

// 显示关于对话框
function showAboutDialog() {
    const aboutText = `${config.APP_NAME} v${config.APP_VERSION}\n\n开发者：${config.APP_CONFIG.APP_INFO.DEVELOPER}\n版本：${config.APP_VERSION}\n更新时间：${config.APP_CONFIG.APP_INFO.UPDATE_DATE}\n\n感谢您的使用！`;
    dialogs.alert("关于", aboutText);
}

// 返回键处理
let lastBackTime = 0;
ui.emitter.on("back_pressed", (e) => {
    try {
        // 如果抽屉菜单是打开的，先关闭抽屉菜单
        if (isDrawerOpen) {
            closeDrawer();
            e.consumed = true; // 阻止默认返回行为
            return;
        }
        
        // 如果悬浮菜单是展开的，先收起悬浮菜单
        if (isFloatyExpanded) {
            collapseFloatyMenu();
            e.consumed = true; // 阻止默认返回行为
            return;
        }
        
        // 如果当前在主页面且已登录，返回到登录页面
        // if (isLoggedIn && ui.homePage.getVisibility() === 0) {
        //     dialogs.confirm("退出登录", "确定要退出登录返回到登录页面吗？")
        //         .then(result => {
        //             if (result) {
        //                 performLogout();
        //             }
        //         });
        //     e.consumed = true; // 阻止默认返回行为
        //     return;
        // }
        
        // 双击返回键退出应用
        const currentTime = Date.now();
        if (currentTime - lastBackTime < 2000) {
            // 2秒内双击返回键，退出应用
            toast("再见！");
            // 清理资源
            if (floatyWindow) {
                removeFloatyWindow();
            }
            // 延迟一点退出，让toast显示
            setTimeout(() => {
                exit();
            }, 500);
        } else {
            // 第一次按返回键，提示用户
            lastBackTime = currentTime;
            toast("再按一次返回键退出应用");
            e.consumed = true; // 阻止默认返回行为
        }
    } catch (error) {
        logger.error("UI", "返回键处理失败", { error: error.message, stack: error.stack });
        // 如果处理失败，允许默认行为
    }
});

// 程序启动时的初始化
ui.run(() => {
    // 记录应用启动
    logger.info("App", config.APP_NAME + "启动", {
        version: config.APP_VERSION,
        deviceModel: device.model || "Unknown",
        systemVersion: "Android " + (device.release || "Unknown"),
        screenSize: device.width + "x" + device.height,
        startTime: new Date().toISOString()
    });
    
    initializeUI();
    initMenuButtons(); // 初始化菜单按钮事件
    updateAccessibilitySwitch(); // 更新无障碍开关状态
    updateFloatySwitch(); // 更新浮动窗口开关状态
    
    logger.info("App", "应用初始化完成");
});

// 添加全局异常处理
events.on("uncaught_exception", function(e) {
    logger.error("App", "未捕获的异常", {
        error: e.message,
        stack: e.stack,
        timestamp: new Date().toISOString()
    });
});

// 添加退出处理
events.on("exit", function() {
    logger.info("App", "应用正在退出");
    logger.getRawLogger().flush(); // 确保日志被写入
});

