package com.ruoyi.script.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 卡密登录返回视图对象
 *
 * @author ruoyi
 * @date 2024-01-01
 */
public class CardLoginVo {

    /**
     * 卡密token
     */
    private String cardToken;

    /**
     * 卡密
     */
    private String cardNo;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 剩余天数
     */
    private Long remainingDays;

    /**
     * 可绑定设备数
     */
    private String deviceSize;

    /**
     * 关联的游戏列表
     */
    private List<GameInfo> games;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    // Getters and Setters

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Long getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(Long remainingDays) {
        this.remainingDays = remainingDays;
    }

    public String getDeviceSize() {
        return deviceSize;
    }

    public void setDeviceSize(String deviceSize) {
        this.deviceSize = deviceSize;
    }

    public List<GameInfo> getGames() {
        return games;
    }

    public void setGames(List<GameInfo> games) {
        this.games = games;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * 游戏信息
     */
    public static class GameInfo {
        private Long gameId;
        private String gameTitle;

        public Long getGameId() {
            return gameId;
        }

        public void setGameId(Long gameId) {
            this.gameId = gameId;
        }

        public String getGameTitle() {
            return gameTitle;
        }

        public void setGameTitle(String gameTitle) {
            this.gameTitle = gameTitle;
        }
    }
}