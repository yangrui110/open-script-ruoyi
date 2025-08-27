package com.ruoyi.script.domain.dto;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * 脚本登录用户适配器
 * 将ScriptLoginUser适配为标准的LoginUser，以便与若依JWT系统兼容
 * 
 * @author ruoyi
 */
public class ScriptLoginUserAdapter extends LoginUser
{
    private static final long serialVersionUID = 1L;

    /**
     * 原始的脚本登录用户对象
     */
    private ScriptLoginUser scriptLoginUser;

    public ScriptLoginUserAdapter(ScriptLoginUser scriptLoginUser)
    {
        this.scriptLoginUser = scriptLoginUser;
        
        // 映射基本属性
        this.setToken(scriptLoginUser.getToken());
        this.setLoginTime(scriptLoginUser.getLoginTime());
        this.setExpireTime(scriptLoginUser.getExpireTime());
        this.setIpaddr(scriptLoginUser.getIpaddr());
        this.setLoginLocation(scriptLoginUser.getLoginLocation());
        this.setBrowser(scriptLoginUser.getBrowser());
        this.setOs(scriptLoginUser.getOs());
        this.setPermissions(scriptLoginUser.getPermissions());
        
        // 创建一个虚拟的SysUser对象
        SysUser virtualUser = new SysUser();
        virtualUser.setUserId(-1L); // 使用负数表示脚本用户
        virtualUser.setUserName(scriptLoginUser.getScriptCard().getCardNo());
        virtualUser.setNickName("脚本用户-" + scriptLoginUser.getScriptCard().getCardNo());
        this.setUser(virtualUser);
        this.setUserId(-1L);
        this.setDeptId(-1L);
    }

    public ScriptLoginUser getScriptLoginUser()
    {
        return scriptLoginUser;
    }

    @Override
    public String getUsername()
    {
        return scriptLoginUser.getUsername();
    }

    @Override
    public String getPassword()
    {
        return scriptLoginUser.getPassword();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return scriptLoginUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return scriptLoginUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return scriptLoginUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled()
    {
        return scriptLoginUser.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return scriptLoginUser.getAuthorities();
    }
}