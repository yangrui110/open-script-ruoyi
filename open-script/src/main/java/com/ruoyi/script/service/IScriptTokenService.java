package com.ruoyi.script.service;

import javax.servlet.http.HttpServletRequest;
import com.ruoyi.script.domain.dto.ScriptLoginUser;

/**
 * 脚本Token验证服务接口
 * 
 * @author ruoyi
 */
public interface IScriptTokenService
{
    /**
     * 获取用户身份信息
     * 
     * @return 用户信息
     */
    public ScriptLoginUser getLoginUser(HttpServletRequest request);

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(ScriptLoginUser loginUser);

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token);

    /**
     * 创建令牌
     * 
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(ScriptLoginUser loginUser);

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     * 
     * @param loginUser 登录信息
     * @return 令牌
     */
    public void verifyToken(ScriptLoginUser loginUser);

    /**
     * 刷新令牌有效期
     * 
     * @param loginUser 登录信息
     */
    public void refreshToken(ScriptLoginUser loginUser);

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    public String getToken(HttpServletRequest request);

    /**
     * 获取当前token值
     *
     * @return token
     */
    public String getTokenValue();

    /**
     * 根据token获取卡密信息
     *
     * @param token token
     * @return 卡密信息
     */
    public com.ruoyi.script.domain.vo.CardLoginVo getCardInfo(String token);
}