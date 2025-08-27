package com.ruoyi.script.domain.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 批量日志上传DTO
 * 
 * @author ruoyi
 */
public class LogBatchDto
{
    /** 应用版本 */
    @NotBlank(message = "应用版本不能为空")
    @Size(max = 50, message = "应用版本长度不能超过50个字符")
    private String appVersion;

    /** 日志列表 */
    @Valid
    @NotEmpty(message = "日志列表不能为空")
    private List<LogEntryDto> logs;

    public String getAppVersion()
    {
        return appVersion;
    }

    public void setAppVersion(String appVersion)
    {
        this.appVersion = appVersion;
    }

    public List<LogEntryDto> getLogs()
    {
        return logs;
    }

    public void setLogs(List<LogEntryDto> logs)
    {
        this.logs = logs;
    }
}