package com.ruoyi.web.controller.script;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.validation.Valid;

import com.ruoyi.common.annotation.ApiEncrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;

import com.ruoyi.script.domain.ScriptLog;
import com.ruoyi.script.domain.dto.LogBatchDto;
import com.ruoyi.script.domain.dto.LogEntryDto;
import com.ruoyi.script.domain.dto.LoginRequest;
import com.ruoyi.script.domain.vo.CardLoginVo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import com.ruoyi.script.domain.dto.ScriptLoginUser;
import com.ruoyi.script.service.IScriptLoginService;
import com.ruoyi.script.service.IScriptLogService;
import com.ruoyi.script.service.IScriptTokenService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.script.domain.dto.ScriptLoginUserAdapter;
import com.ruoyi.script.domain.vo.CardLoginVo;
import com.ruoyi.script.domain.vo.ScriptVersionControlVo;
import com.ruoyi.script.service.IScriptVersionControlService;

/**
 * 开放API控制器（对外接口）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/open-api/script/")
public class OpenApiController
{
    @Autowired
    private IScriptLoginService scriptLoginService;

    @Autowired
    private IScriptTokenService scriptTokenService;

    @Autowired
    private IScriptLogService scriptLogService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private IScriptVersionControlService scriptVersionControlService;
    
    // 令牌自定义标识
    @Value("${token.header}")
    private String header;


    /**
     * 卡密登录接口
     */
    @PostMapping("/login")
    @ApiEncrypt(response = true)
    public AjaxResult login(@Validated @RequestBody LoginRequest request)
    {
        try
        {
            // 1. 执行登录验证
            ScriptLoginUser scriptLoginUser = scriptLoginService.login(request);

            // 2. 生成JWT令牌（使用标准TokenService）
            ScriptLoginUserAdapter adapter = new ScriptLoginUserAdapter(scriptLoginUser);
            String token = tokenService.createToken(adapter);

            // 3. 构建新的CardLoginVo响应数据
            CardLoginVo cardLoginVo = new CardLoginVo();
            cardLoginVo.setCardToken(token);
            cardLoginVo.setCardNo(scriptLoginUser.getScriptCard().getCardNo());
            
            // 设置过期时间
            if (scriptLoginUser.getScriptCard().getExpireTime() != null) {
                cardLoginVo.setExpireTime(scriptLoginUser.getScriptCard().getExpireTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
                
                // 计算剩余天数
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expireDateTime = cardLoginVo.getExpireTime();
                long remainingDays = ChronoUnit.DAYS.between(now, expireDateTime);
                cardLoginVo.setRemainingDays(Math.max(0, remainingDays));
            }
            
            cardLoginVo.setDeviceSize(scriptLoginUser.getScriptCard().getDeviceSize());
            cardLoginVo.setLoginTime(LocalDateTime.now());
            
            // 转换游戏列表
            if (scriptLoginUser.getGameList() != null) {
                List<CardLoginVo.GameInfo> gameInfos = scriptLoginUser.getGameList().stream()
                    .map(game -> {
                        CardLoginVo.GameInfo gameInfo = new CardLoginVo.GameInfo();
                        gameInfo.setGameId(game.getGameId());
                        gameInfo.setGameTitle(game.getGameTitle());
                        return gameInfo;
                    }).collect(Collectors.toList());
                cardLoginVo.setGames(gameInfos);
            }

            return AjaxResult.success(cardLoginVo);
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息（需要JWT认证）
     */
    @GetMapping("/userinfo")
    @ApiEncrypt(response = true)
    public AjaxResult getUserInfo(HttpServletRequest request)
    {
        // 使用标准TokenService获取用户信息
        com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null)
        {
            return AjaxResult.error(401, "未授权访问");
        }

        // 验证令牌有效性并刷新
        tokenService.verifyToken(loginUser);

        // 如果是ScriptLoginUserAdapter，返回原始的ScriptLoginUser
        if (loginUser instanceof ScriptLoginUserAdapter)
        {
            ScriptLoginUser scriptLoginUser = ((ScriptLoginUserAdapter) loginUser).getScriptLoginUser();
            return AjaxResult.success(scriptLoginUser);
        }
        
        return AjaxResult.success(loginUser);
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    @ApiEncrypt(response = true)
    public AjaxResult logout(HttpServletRequest request)
    {
        // 使用标准TokenService进行登出
        com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser != null)
        {
            String token = loginUser.getToken();
            tokenService.delLoginUser(token);
        }
        return AjaxResult.success("登出成功");
    }

    /**
     * 验证token接口
     */
    @PostMapping("/verify")
    @ApiEncrypt(response = true)
    public AjaxResult verifyToken(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestHeader(value = "Card-Token", required = false) String cardTokenHeader,
                                 HttpServletRequest request)
    {
        // 支持两种token格式：Authorization Bearer token 和 Card-Token
        final String cardToken;
        if (authorization != null && authorization.startsWith("Bearer "))
        {
            cardToken = authorization.substring(7);
        }
        else if (cardTokenHeader != null)
        {
            cardToken = cardTokenHeader;
        }
        else
        {
            cardToken = null;
        }

        if (cardToken == null)
        {
            return AjaxResult.error(401, "缺少认证token");
        }

        try
        {
            // 创建一个包装请求，将token设置到正确的头部
            HttpServletRequest tokenRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if (header.equals(name)) {
                        return Constants.TOKEN_PREFIX + cardToken;
                    }
                    return super.getHeader(name);
                }
            };
            
            // 使用标准TokenService验证token
            com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.getLoginUser(tokenRequest);
            if (loginUser == null)
            {
                return AjaxResult.error(401, "token无效");
            }

            // 验证令牌有效性并刷新
            tokenService.verifyToken(loginUser);

            // 如果是ScriptLoginUserAdapter，获取原始的ScriptLoginUser构建返回信息
            if (loginUser instanceof ScriptLoginUserAdapter)
            {
                ScriptLoginUser scriptLoginUser = ((ScriptLoginUserAdapter) loginUser).getScriptLoginUser();
                
                // 构建CardLoginVo响应数据
                CardLoginVo cardLoginVo = new CardLoginVo();
                cardLoginVo.setCardToken(cardToken);
                cardLoginVo.setCardNo(scriptLoginUser.getScriptCard().getCardNo());
                
                // 设置过期时间
                if (scriptLoginUser.getScriptCard().getExpireTime() != null) {
                    cardLoginVo.setExpireTime(scriptLoginUser.getScriptCard().getExpireTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                    
                    // 计算剩余天数
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expireDateTime = cardLoginVo.getExpireTime();
                    long remainingDays = ChronoUnit.DAYS.between(now, expireDateTime);
                    cardLoginVo.setRemainingDays(Math.max(0, remainingDays));
                }
                
                cardLoginVo.setDeviceSize(scriptLoginUser.getScriptCard().getDeviceSize());
                cardLoginVo.setLoginTime(LocalDateTime.now());
                
                // 转换游戏列表
                if (scriptLoginUser.getGameList() != null) {
                    List<CardLoginVo.GameInfo> gameInfos = scriptLoginUser.getGameList().stream()
                        .map(game -> {
                            CardLoginVo.GameInfo gameInfo = new CardLoginVo.GameInfo();
                            gameInfo.setGameId(game.getGameId());
                            gameInfo.setGameTitle(game.getGameTitle());
                            return gameInfo;
                        }).collect(Collectors.toList());
                    cardLoginVo.setGames(gameInfos);
                }

                return AjaxResult.success(cardLoginVo);
            }
            
            return AjaxResult.success("验证成功");
        }
        catch (Exception e)
        {
            return AjaxResult.error(401, "token验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取卡密信息接口
     */
    @GetMapping("/card-info")
    @ApiEncrypt(response = true)
    public AjaxResult getCardInfo()
    {
        // 从当前上下文获取token
        String cardToken = scriptTokenService.getTokenValue();
        if (cardToken == null)
        {
            return AjaxResult.error(401, "未登录");
        }
        
        CardLoginVo loginVo = scriptTokenService.getCardInfo(cardToken);
        if (loginVo == null)
        {
            return AjaxResult.error(401, "获取卡密信息失败");
        }
        
        return AjaxResult.success(loginVo);
    }

    /**
     * 获取游戏数据接口（需要卡密认证）
     * 演示如何在业务接口中使用卡密认证
     */
    @GetMapping("/game-data/{gameId}")
    @ApiEncrypt(response = true)
    public AjaxResult getGameData(@PathVariable Long gameId, HttpServletRequest request)
    {
        // 使用标准TokenService获取用户信息
        com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null)
        {
            return AjaxResult.error(401, "未登录");
        }

        // 验证令牌有效性并刷新
        tokenService.verifyToken(loginUser);

        // 检查是否是脚本用户
        if (!(loginUser instanceof ScriptLoginUserAdapter))
        {
            return AjaxResult.error(403, "非脚本用户无权访问");
        }

        ScriptLoginUser scriptLoginUser = ((ScriptLoginUserAdapter) loginUser).getScriptLoginUser();

        // 检查是否有该游戏的权限
        boolean hasPermission = scriptLoginUser.getGameList() != null && 
            scriptLoginUser.getGameList().stream()
                .anyMatch(game -> gameId.equals(game.getGameId()));

        if (!hasPermission)
        {
            return AjaxResult.error(403, "无权限访问该游戏");
        }

        // 这里可以返回具体的游戏数据
        String gameData = "游戏数据：" + gameId + "，卡号：" + scriptLoginUser.getScriptCard().getCardNo();
        return AjaxResult.success(gameData);
    }

    /**
     * 获取指定游戏的最新版本
     */
    @GetMapping("/latest/{gameId}")
    @ApiEncrypt(response = true)
    public AjaxResult getLatestVersion(@PathVariable Long gameId, HttpServletRequest request)
    {
        // 使用标准TokenService获取用户信息（需要认证）
        com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null)
        {
            return AjaxResult.error(401, "未登录");
        }

        // 验证令牌有效性并刷新
        tokenService.verifyToken(loginUser);

        // 检查是否是脚本用户
        if (!(loginUser instanceof ScriptLoginUserAdapter))
        {
            return AjaxResult.error(403, "非脚本用户无权访问");
        }

        ScriptLoginUser scriptLoginUser = ((ScriptLoginUserAdapter) loginUser).getScriptLoginUser();

        // 检查是否有该游戏的权限
        boolean hasPermission = scriptLoginUser.getGameList() != null && 
            scriptLoginUser.getGameList().stream()
                .anyMatch(game -> gameId.equals(game.getGameId()));

        if (!hasPermission)
        {
            return AjaxResult.error(403, "无权限访问该游戏");
        }

        // 获取最新版本信息
        ScriptVersionControlVo latestVersion = scriptVersionControlService.getLatestVersion(gameId);
        if (latestVersion == null)
        {
            return AjaxResult.error(404, "该游戏暂无版本信息");
        }

        return AjaxResult.success(latestVersion);
    }

    /**
     * 批量上传日志接口
     * 用于AutoJS脚本上传执行日志
     */
    @PostMapping("/logs/upload")
    @ApiEncrypt(response = true)
    public AjaxResult uploadLogs(@Valid @RequestBody LogBatchDto logBatchDto, HttpServletRequest request)
    {
        try
        {
            // 批量保存日志
            for (LogEntryDto logEntry : logBatchDto.getLogs())
            {
                ScriptLog scriptLog = new ScriptLog();
                scriptLog.setLevel(logEntry.getLevel());
                scriptLog.setTag(logEntry.getTag());
                scriptLog.setMessage(logEntry.getMessage());
                scriptLog.setExtra(logEntry.getExtra());
                scriptLog.setAndroidId(logEntry.getDeviceId());
                scriptLog.setRemark("AutoJS脚本上传 - " + logBatchDto.getAppVersion());
                
                scriptLogService.insertScriptLog(scriptLog);
            }
            
            return AjaxResult.success("日志上传成功，共 " + logBatchDto.getLogs().size() + " 条");
        }
        catch (Exception e)
        {
            return AjaxResult.error("日志上传失败：" + e.getMessage());
        }
    }
}
