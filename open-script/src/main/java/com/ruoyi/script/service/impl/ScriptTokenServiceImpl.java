package com.ruoyi.script.service.impl;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.ruoyi.common.utils.uuid.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.script.domain.dto.ScriptLoginUser;
import com.ruoyi.script.domain.dto.ScriptLoginUserAdapter;
import com.ruoyi.script.service.IScriptTokenService;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 脚本token验证处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptTokenServiceImpl implements IScriptTokenService
{
    private static final Logger log = LoggerFactory.getLogger(ScriptTokenServiceImpl.class);

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     * 
     * @return 用户信息
     */
    @Override
    public ScriptLoginUser getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                ScriptLoginUserAdapter adapter = redisCache.getCacheObject(userKey);
                return adapter != null ? adapter.getScriptLoginUser() : null;
            }
            catch (Exception e)
            {
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    @Override
    public void setLoginUser(ScriptLoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息
     */
    @Override
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建令牌
     * 
     * @param loginUser 用户信息
     * @return 令牌
     */
    @Override
    public String createToken(ScriptLoginUser loginUser)
    {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     * 
     * @param loginUser 登录信息
     * @return 令牌
     */
    @Override
    public void verifyToken(ScriptLoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     * 
     * @param loginUser 登录信息
     */
    @Override
    public void refreshToken(ScriptLoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser适配器缓存，以便与标准JWT系统兼容
        String userKey = getTokenKey(loginUser.getToken());
        ScriptLoginUserAdapter adapter = new ScriptLoginUserAdapter(loginUser);
        redisCache.setCacheObject(userKey, adapter, expireTime, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 设置用户代理信息
     * 
     * @param loginUser 登录信息
     */
    public void setUserAgent(ScriptLoginUser loginUser)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    @Override
    public String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取当前token值
     *
     * @return token
     */
    @Override
    public String getTokenValue()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null)
        {
            return getToken(request);
        }
        return null;
    }

    /**
     * 根据token获取卡密信息
     *
     * @param token token
     * @return 卡密信息
     */
    @Override
    public com.ruoyi.script.domain.vo.CardLoginVo getCardInfo(String token)
    {
        try
        {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            String userKey = getTokenKey(uuid);
            ScriptLoginUserAdapter adapter = redisCache.getCacheObject(userKey);
            
            if (adapter != null && adapter.getScriptLoginUser() != null)
            {
                ScriptLoginUser scriptLoginUser = adapter.getScriptLoginUser();
                
                // 构建CardLoginVo
                com.ruoyi.script.domain.vo.CardLoginVo cardLoginVo = new com.ruoyi.script.domain.vo.CardLoginVo();
                cardLoginVo.setCardToken(token);
                cardLoginVo.setCardNo(scriptLoginUser.getScriptCard().getCardNo());
                
                // 转换过期时间
                if (scriptLoginUser.getScriptCard().getExpireTime() != null)
                {
                    cardLoginVo.setExpireTime(scriptLoginUser.getScriptCard().getExpireTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    
                    // 计算剩余天数
                    long remainingDays = java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        cardLoginVo.getExpireTime()
                    ).toDays();
                    cardLoginVo.setRemainingDays(Math.max(0, remainingDays));
                }
                
                cardLoginVo.setDeviceSize(scriptLoginUser.getScriptCard().getDeviceSize());
                
                // 转换游戏列表
                if (scriptLoginUser.getGameList() != null)
                {
                    java.util.List<com.ruoyi.script.domain.vo.CardLoginVo.GameInfo> games = 
                        scriptLoginUser.getGameList().stream()
                            .map(game -> {
                                com.ruoyi.script.domain.vo.CardLoginVo.GameInfo gameInfo = 
                                    new com.ruoyi.script.domain.vo.CardLoginVo.GameInfo();
                                gameInfo.setGameId(game.getGameId());
                                gameInfo.setGameTitle(game.getGameTitle());
                                return gameInfo;
                            })
                            .collect(java.util.stream.Collectors.toList());
                    cardLoginVo.setGames(games);
                }
                
                // 设置登录时间
                if (scriptLoginUser.getLoginTime() != null)
                {
                    cardLoginVo.setLoginTime(java.time.Instant.ofEpochMilli(scriptLoginUser.getLoginTime())
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                }
                
                return cardLoginVo;
            }
        }
        catch (Exception e)
        {
            log.error("获取卡密信息异常'{}'", e.getMessage());
        }
        return null;
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}