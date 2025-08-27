package com.ruoyi.script.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 脚本日志表 script_log
 * 
 * @author ruoyi
 */
public class ScriptLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** androidId */
    @Excel(name = "设备ID")
    private String androidId;

    /** 日志级别(INFO、WARN、ERROR) */
    @Excel(name = "日志级别", readConverterExp = "INFO=信息,WARN=警告,ERROR=错误")
    private String level;

    /** 标签类型 */
    @Excel(name = "标签类型")
    private String tag;

    /** 具体信息 */
    @Excel(name = "具体信息")
    private String message;

    /** 额外的信息 */
    @Excel(name = "额外信息")
    private String extra;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 创建部门 */
    private Long createDept;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setAndroidId(String androidId)
    {
        this.androidId = androidId;
    }

    @NotBlank(message = "设备ID不能为空")
    @Size(min = 0, max = 40, message = "设备ID长度不能超过40个字符")
    public String getAndroidId()
    {
        return androidId;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    @NotBlank(message = "日志级别不能为空")
    @Size(min = 0, max = 20, message = "日志级别长度不能超过20个字符")
    public String getLevel()
    {
        return level;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @NotBlank(message = "标签类型不能为空")
    @Size(min = 0, max = 200, message = "标签类型长度不能超过200个字符")
    public String getTag()
    {
        return tag;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @NotBlank(message = "具体信息不能为空")
    @Size(min = 0, max = 200, message = "具体信息长度不能超过200个字符")
    public String getMessage()
    {
        return message;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    @NotBlank(message = "额外信息不能为空")
    public String getExtra()
    {
        return extra;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setCreateDept(Long createDept)
    {
        this.createDept = createDept;
    }

    public Long getCreateDept()
    {
        return createDept;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("androidId", getAndroidId())
            .append("level", getLevel())
            .append("tag", getTag())
            .append("message", getMessage())
            .append("extra", getExtra())
            .append("delFlag", getDelFlag())
            .append("createDept", getCreateDept())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}