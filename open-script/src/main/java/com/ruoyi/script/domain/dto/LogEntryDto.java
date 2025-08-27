package com.ruoyi.script.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 日志条目DTO
 * 
 * @author ruoyi
 */
public class LogEntryDto
{
    /** 日志级别 */
    @NotBlank(message = "日志级别不能为空")
    @Size(max = 20, message = "日志级别长度不能超过20个字符")
    private String level;

    /** 标签 */
    @NotBlank(message = "标签不能为空")
    @Size(max = 200, message = "标签长度不能超过200个字符")
    private String tag;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 200, message = "消息内容长度不能超过200个字符")
    private String message;

    /** 额外信息 */
    private String extra;

    /** 设备ID */
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 40, message = "设备ID长度不能超过40个字符")
    private String deviceId;

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }
}