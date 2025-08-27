package com.ruoyi.script.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 卡号关联游戏 script_card_game
 * 
 * @author ruoyi
 */
public class ScriptCardGame extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 卡号 */
    private String cardNo;

    /** 游戏ID */
    private Long gameId;

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

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    public String getCardNo()
    {
        return cardNo;
    }

    public void setGameId(Long gameId)
    {
        this.gameId = gameId;
    }

    public Long getGameId()
    {
        return gameId;
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
            .append("cardNo", getCardNo())
            .append("gameId", getGameId())
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