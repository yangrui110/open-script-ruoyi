package com.ruoyi.script.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设备表 script_device
 * 
 * @author ruoyi
 */
public class ScriptDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 设备的Android ID */
    @Excel(name = "设备ID")
    private String deviceAndroidId;

    /** 宽 */
    @Excel(name = "屏幕宽度")
    private Integer deviceWidth;

    /** 高 */
    @Excel(name = "屏幕高度")
    private Integer deviceHeight;

    /** 修订版本号 */
    @Excel(name = "修订版本号")
    private String deviceBuildId;

    /** 主板型号 */
    @Excel(name = "主板型号")
    private String deviceBroad;

    /** 厂商品牌 */
    @Excel(name = "厂商品牌")
    private String deviceBrand;

    /** 设备在工业设计中的名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 设备型号 */
    @Excel(name = "设备型号")
    private String deviceModel;

    /** 安卓系统API版本 */
    @Excel(name = "API版本")
    private String deviceSdkInt;

    /** 设备IMEI */
    @Excel(name = "设备IMEI")
    private String deviceIMEI;

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

    public void setDeviceWidth(Integer deviceWidth)
    {
        this.deviceWidth = deviceWidth;
    }

    public Integer getDeviceWidth()
    {
        return deviceWidth;
    }

    public void setDeviceHeight(Integer deviceHeight)
    {
        this.deviceHeight = deviceHeight;
    }

    public Integer getDeviceHeight()
    {
        return deviceHeight;
    }

    public void setDeviceBuildId(String deviceBuildId)
    {
        this.deviceBuildId = deviceBuildId;
    }

    @Size(min = 0, max = 20, message = "修订版本号长度不能超过20个字符")
    public String getDeviceBuildId()
    {
        return deviceBuildId;
    }

    public void setDeviceBroad(String deviceBroad)
    {
        this.deviceBroad = deviceBroad;
    }

    @Size(min = 0, max = 20, message = "主板型号长度不能超过20个字符")
    public String getDeviceBroad()
    {
        return deviceBroad;
    }

    public void setDeviceBrand(String deviceBrand)
    {
        this.deviceBrand = deviceBrand;
    }

    @Size(min = 0, max = 30, message = "厂商品牌长度不能超过30个字符")
    public String getDeviceBrand()
    {
        return deviceBrand;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    @Size(min = 0, max = 30, message = "设备名称长度不能超过30个字符")
    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceModel(String deviceModel)
    {
        this.deviceModel = deviceModel;
    }

    @Size(min = 0, max = 30, message = "设备型号长度不能超过30个字符")
    public String getDeviceModel()
    {
        return deviceModel;
    }

    public void setDeviceSdkInt(String deviceSdkInt)
    {
        this.deviceSdkInt = deviceSdkInt;
    }

    @Size(min = 0, max = 10, message = "API版本长度不能超过10个字符")
    public String getDeviceSdkInt()
    {
        return deviceSdkInt;
    }

    public void setDeviceIMEI(String deviceIMEI)
    {
        this.deviceIMEI = deviceIMEI;
    }

    @Size(min = 0, max = 30, message = "设备IMEI长度不能超过30个字符")
    public String getDeviceIMEI()
    {
        return deviceIMEI;
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
            .append("deviceAndroidId", getDeviceAndroidId())
            .append("deviceWidth", getDeviceWidth())
            .append("deviceHeight", getDeviceHeight())
            .append("deviceBuildId", getDeviceBuildId())
            .append("deviceBroad", getDeviceBroad())
            .append("deviceBrand", getDeviceBrand())
            .append("deviceName", getDeviceName())
            .append("deviceModel", getDeviceModel())
            .append("deviceSdkInt", getDeviceSdkInt())
            .append("deviceIMEI", getDeviceIMEI())
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