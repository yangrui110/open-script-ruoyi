package com.ruoyi.script.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * @author ruoyi
 */
public class LoginRequest
{
    /** 卡密 */
    @NotBlank(message = "卡密不能为空")
    @Size(min = 1, max = 20, message = "卡密长度必须在1-20个字符之间")
    private String cardNo;

    /** 设备的Android ID */
    @NotBlank(message = "设备ID不能为空")
    @Size(min = 1, max = 30, message = "设备ID长度必须在1-30个字符之间")
    private String deviceAndroidId;

    /** 屏幕宽度 */
    private Integer deviceWidth;

    /** 屏幕高度 */
    private Integer deviceHeight;

    /** 修订版本号 */
    private String deviceBuildId;

    /** 主板型号 */
    private String deviceBroad;

    /** 厂商品牌 */
    private String deviceBrand;

    /** 设备名称 */
    private String deviceName;

    /** 设备型号 */
    private String deviceModel;

    /** 安卓系统API版本 */
    private String deviceSdkInt;

    /** 设备IMEI */
    private String deviceIMEI;

    public String getCardNo()
    {
        return cardNo;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    public String getDeviceAndroidId()
    {
        return deviceAndroidId;
    }

    public void setDeviceAndroidId(String deviceAndroidId)
    {
        this.deviceAndroidId = deviceAndroidId;
    }

    public Integer getDeviceWidth()
    {
        return deviceWidth;
    }

    public void setDeviceWidth(Integer deviceWidth)
    {
        this.deviceWidth = deviceWidth;
    }

    public Integer getDeviceHeight()
    {
        return deviceHeight;
    }

    public void setDeviceHeight(Integer deviceHeight)
    {
        this.deviceHeight = deviceHeight;
    }

    public String getDeviceBuildId()
    {
        return deviceBuildId;
    }

    public void setDeviceBuildId(String deviceBuildId)
    {
        this.deviceBuildId = deviceBuildId;
    }

    public String getDeviceBroad()
    {
        return deviceBroad;
    }

    public void setDeviceBroad(String deviceBroad)
    {
        this.deviceBroad = deviceBroad;
    }

    public String getDeviceBrand()
    {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand)
    {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceModel()
    {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel)
    {
        this.deviceModel = deviceModel;
    }

    public String getDeviceSdkInt()
    {
        return deviceSdkInt;
    }

    public void setDeviceSdkInt(String deviceSdkInt)
    {
        this.deviceSdkInt = deviceSdkInt;
    }

    public String getDeviceIMEI()
    {
        return deviceIMEI;
    }

    public void setDeviceIMEI(String deviceIMEI)
    {
        this.deviceIMEI = deviceIMEI;
    }
}