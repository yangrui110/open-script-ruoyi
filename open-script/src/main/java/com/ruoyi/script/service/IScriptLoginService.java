package com.ruoyi.script.service;

import com.ruoyi.script.domain.dto.LoginRequest;
import com.ruoyi.script.domain.dto.ScriptLoginUser;

/**
 * 脚本登录服务接口
 * 
 * @author ruoyi
 */
public interface IScriptLoginService
{
    /**
     * 卡密登录验证
     * 
     * @param loginRequest 登录请求信息
     * @return 脚本登录用户
     */
    public ScriptLoginUser login(LoginRequest loginRequest);

    /**
     * 创建登录令牌
     * 
     * @param scriptLoginUser 脚本登录用户
     * @return 令牌
     */
    public String createToken(ScriptLoginUser scriptLoginUser);

    /**
     * 验证令牌有效性
     * 
     * @param scriptLoginUser 脚本登录用户
     */
    public void verifyToken(ScriptLoginUser scriptLoginUser);

    /**
     * 刷新令牌
     * 
     * @param scriptLoginUser 脚本登录用户
     */
    public void refreshToken(ScriptLoginUser scriptLoginUser);

    /**
     * 删除用户缓存记录
     * 
     * @param token 令牌
     */
    public void logout(String token);
}