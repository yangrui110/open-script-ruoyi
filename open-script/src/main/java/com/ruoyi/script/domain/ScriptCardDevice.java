package com.ruoyi.script.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 卡密关联设备表 script_card_device
 * 
 * @author ruoyi
 */
public class ScriptCardDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 卡密 */
    @Excel(name = "卡密")
    private String cardNo;

    /** 设备的Android ID */
    @Excel(name = "设备Android ID")
    private String deviceAndroidId;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 创建部门 */
    private Long createDept;

    /** 设备信息（用于关联查询显示） */
    private ScriptDevice deviceInfo;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    @NotBlank(message = "卡密不能为空")
    @Size(min = 0, max = 20, message = "卡密长度不能超过20个字符")
    public String getCardNo()
    {
        return cardNo;
    }

    public void setDeviceAndroidId(String deviceAndroidId)
    {
        this.deviceAndroidId = deviceAndroidId;
    }

    @NotBlank(message = "设备Android ID不能为空")
    @Size(min = 0, max = 30, message = "设备Android ID长度不能超过30个字符")
    public String getDeviceAndroidId()
    {
        return deviceAndroidId;
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

    public void setDeviceInfo(ScriptDevice deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

    public ScriptDevice getDeviceInfo()
    {
        return deviceInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("cardNo", getCardNo())
            .append("deviceAndroidId", getDeviceAndroidId())
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