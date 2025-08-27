package com.ruoyi.script.domain.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.alibaba.fastjson2.annotation.JSONField;
import com.ruoyi.script.domain.ScriptCard;
import com.ruoyi.script.domain.ScriptCardGame;

/**
 * 脚本登录用户身份权限
 * 
 * @author ruoyi
 */
public class ScriptLoginUser implements UserDetails
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 卡密信息
     */
    private ScriptCard scriptCard;

    /**
     * 设备Android ID
     */
    private String deviceAndroidId;

    /**
     * 关联的游戏列表
     */
    private List<ScriptCardGame> gameList;

    /**
     * 是否首次绑定
     */
    private boolean firstBinding;

    public ScriptLoginUser()
    {
    }

    public ScriptLoginUser(ScriptCard scriptCard, String deviceAndroidId, Set<String> permissions)
    {
        this.scriptCard = scriptCard;
        this.deviceAndroidId = deviceAndroidId;
        this.permissions = permissions;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    @JSONField(serialize = false)
    @Override
    public String getPassword()
    {
        // 卡密登录不需要密码验证
        return null;
    }

    @Override
    public String getUsername()
    {
        return scriptCard != null ? scriptCard.getCardNo() : null;
    }

    /**
     * 账户是否未过期,过期无法验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /**
     * 指定用户是否解锁,锁定的用户无法进行身份验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked()
    {
        return scriptCard == null || !"1".equals(scriptCard.getStatus());
    }

    /**
     * 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /**
     * 是否可用 ,禁用的用户不能身份验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isEnabled()
    {
        return scriptCard != null && "0".equals(scriptCard.getStatus());
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public ScriptCard getScriptCard()
    {
        return scriptCard;
    }

    public void setScriptCard(ScriptCard scriptCard)
    {
        this.scriptCard = scriptCard;
    }

    public String getDeviceAndroidId()
    {
        return deviceAndroidId;
    }

    public void setDeviceAndroidId(String deviceAndroidId)
    {
        this.deviceAndroidId = deviceAndroidId;
    }

    public List<ScriptCardGame> getGameList()
    {
        return gameList;
    }

    public void setGameList(List<ScriptCardGame> gameList)
    {
        this.gameList = gameList;
    }

    public boolean isFirstBinding()
    {
        return firstBinding;
    }

    public void setFirstBinding(boolean firstBinding)
    {
        this.firstBinding = firstBinding;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return null;
    }
}