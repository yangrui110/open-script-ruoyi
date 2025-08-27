package com.ruoyi.script.domain.dto;

import java.util.List;
import com.ruoyi.script.domain.ScriptCardGame;

/**
 * 登录响应DTO
 * 
 * @author ruoyi
 */
public class LoginResponse
{
    /** 登录状态码 */
    private int code;

    /** 登录消息 */
    private String message;

    /** 访问令牌 */
    private String token;

    /** 卡密 */
    private String cardNo;

    /** 过期时间 */
    private String expireTime;

    /** 状态（0正常 1停用） */
    private String status;

    /** 可绑定设备数 */
    private String deviceSize;

    /** 已绑定设备数 */
    private int boundDeviceCount;

    /** 关联的游戏列表 */
    private List<ScriptCardGame> gameList;

    /** 设备Android ID */
    private String deviceAndroidId;

    /** 是否为首次绑定 */
    private boolean firstBinding;

    public LoginResponse()
    {
    }

    public LoginResponse(int code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public static LoginResponse success(String message)
    {
        return new LoginResponse(200, message);
    }

    public static LoginResponse error(String message)
    {
        return new LoginResponse(500, message);
    }

    public static LoginResponse error(int code, String message)
    {
        return new LoginResponse(code, message);
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getCardNo()
    {
        return cardNo;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    public String getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(String expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDeviceSize()
    {
        return deviceSize;
    }

    public void setDeviceSize(String deviceSize)
    {
        this.deviceSize = deviceSize;
    }

    public int getBoundDeviceCount()
    {
        return boundDeviceCount;
    }

    public void setBoundDeviceCount(int boundDeviceCount)
    {
        this.boundDeviceCount = boundDeviceCount;
    }

    public List<ScriptCardGame> getGameList()
    {
        return gameList;
    }

    public void setGameList(List<ScriptCardGame> gameList)
    {
        this.gameList = gameList;
    }

    public String getDeviceAndroidId()
    {
        return deviceAndroidId;
    }

    public void setDeviceAndroidId(String deviceAndroidId)
    {
        this.deviceAndroidId = deviceAndroidId;
    }

    public boolean isFirstBinding()
    {
        return firstBinding;
    }

    public void setFirstBinding(boolean firstBinding)
    {
        this.firstBinding = firstBinding;
    }
}