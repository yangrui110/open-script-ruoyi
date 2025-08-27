package com.ruoyi.script.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 卡密表 script_card
 * 
 * @author ruoyi
 */
public class ScriptCard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 卡密 */
    @Excel(name = "卡密")
    private String cardNo;

    /** 过期天数 */
    @Excel(name = "过期天数")
    private Integer expireDay;

    /** 价格 */
    @Excel(name = "价格")
    private BigDecimal price;

    /** 实际过期时间（初始为NULL，第一次绑定时赋值） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际过期时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireTime;

    /** 可绑定设备数 */
    @Excel(name = "可绑定设备数")
    private String deviceSize;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    /** 创建部门 */
    private Long createDept;

    /** 关联的游戏ID列表（用于前端传递） */
    private Long[] gameIds;

    /** 关联的游戏列表（用于显示） */
    private List<ScriptCardGame> gameList;

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

    public void setExpireDay(Integer expireDay)
    {
        this.expireDay = expireDay;
    }

    @NotNull(message = "过期天数不能为空")
    public Integer getExpireDay()
    {
        return expireDay;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能小于0")
    public BigDecimal getPrice()
    {
        return price;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setDeviceSize(String deviceSize)
    {
        this.deviceSize = deviceSize;
    }

    public String getDeviceSize()
    {
        return deviceSize;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setCreateDept(Long createDept)
    {
        this.createDept = createDept;
    }

    public Long getCreateDept()
    {
        return createDept;
    }

    public void setGameIds(Long[] gameIds)
    {
        this.gameIds = gameIds;
    }

    public Long[] getGameIds()
    {
        return gameIds;
    }

    public void setGameList(List<ScriptCardGame> gameList)
    {
        this.gameList = gameList;
    }

    public List<ScriptCardGame> getGameList()
    {
        return gameList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("cardNo", getCardNo())
            .append("expireDay", getExpireDay())
            .append("price", getPrice())
            .append("expireTime", getExpireTime())
            .append("deviceSize", getDeviceSize())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("createDept", getCreateDept())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}