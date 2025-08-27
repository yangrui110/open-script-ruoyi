package com.ruoyi.script.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 脚本版本表 script_version_control
 * 
 * @author ruoyi
 */
public class ScriptVersionControl extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 游戏ID */
    @Excel(name = "游戏ID")
    private Long gameId;

    /** 文件地址 */
    @Excel(name = "文件地址")
    private String fileUrl;

    /** 文件类型(0:单js文件,1:zip文件) */
    @Excel(name = "文件类型", readConverterExp = "0=单js文件,1=zip文件")
    private Integer type;

    /** 版本(每次上传。代码版本自动+1) */
    @Excel(name = "版本")
    private Integer version;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 创建部门 */
    private Long createDept;

    /** 游戏名称（用于关联查询显示） */
    private String gameTitle;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setGameId(Long gameId)
    {
        this.gameId = gameId;
    }

    @NotNull(message = "游戏不能为空")
    public Long getGameId()
    {
        return gameId;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    @NotBlank(message = "文件地址不能为空")
    @Size(min = 0, max = 200, message = "文件地址长度不能超过200个字符")
    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    @NotNull(message = "文件类型不能为空")
    public Integer getType()
    {
        return type;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Integer getVersion()
    {
        return version;
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

    public void setGameTitle(String gameTitle)
    {
        this.gameTitle = gameTitle;
    }

    public String getGameTitle()
    {
        return gameTitle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("gameId", getGameId())
            .append("fileUrl", getFileUrl())
            .append("type", getType())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("createDept", getCreateDept())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("gameTitle", getGameTitle())
            .toString();
    }
}