package com.ruoyi.script.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ruoyi.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.script.domain.ScriptCard;
import com.ruoyi.script.domain.ScriptCardGame;
import com.ruoyi.script.domain.ScriptDevice;
import com.ruoyi.script.domain.dto.LoginRequest;
import com.ruoyi.script.domain.dto.ScriptLoginUser;
import com.ruoyi.script.service.IScriptCardService;
import com.ruoyi.script.service.IScriptCardGameService;
import com.ruoyi.script.service.IScriptDeviceService;
import com.ruoyi.script.service.IScriptCardDeviceService;
import com.ruoyi.script.service.IScriptLoginService;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 脚本登录服务实现
 * 
 * @author ruoyi
 */
@Service
public class ScriptLoginServiceImpl implements IScriptLoginService
{
    @Autowired
    private IScriptCardService scriptCardService;

    @Autowired
    private IScriptCardGameService scriptCardGameService;

    @Autowired
    private IScriptDeviceService scriptDeviceService;

    @Autowired
    private IScriptCardDeviceService scriptCardDeviceService;

    @Autowired
    private RedisCache redisCache;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    /**
     * 卡密登录验证
     * 
     * @param loginRequest 登录请求信息
     * @return 脚本登录用户
     */
    @Override
    @Transactional
    public ScriptLoginUser login(LoginRequest loginRequest)
    {
        // 1. 验证卡密是否存在
        ScriptCard scriptCard = scriptCardService.selectScriptCardByCardNo(loginRequest.getCardNo());
        if (scriptCard == null)
        {
            throw new RuntimeException("卡密不存在");
        }

        // 2. 验证卡密状态
        if (!"0".equals(scriptCard.getStatus()))
        {
            throw new RuntimeException("卡密已被停用");
        }

        // 3. 验证卡密是否已过期
        if (scriptCard.getExpireTime() != null && scriptCard.getExpireTime().before(new Date()))
        {
            throw new RuntimeException("卡密已过期");
        }

        // 4. 检查设备绑定数量限制
        int deviceSize = Integer.parseInt(scriptCard.getDeviceSize() == null ? "1" : scriptCard.getDeviceSize());
        int boundDeviceCount = scriptCardDeviceService.countDevicesByCardNo(loginRequest.getCardNo());
        
        // 检查当前设备是否已绑定
        boolean isDeviceBound = scriptCardDeviceService.selectScriptCardDeviceByCardNoAndAndroidId(
            loginRequest.getCardNo(), loginRequest.getDeviceAndroidId()) != null;
        
        if (!isDeviceBound && boundDeviceCount >= deviceSize)
        {
            throw new RuntimeException("设备绑定数量已达上限，当前已绑定" + boundDeviceCount + "台设备");
        }

        // 5. 保存或更新设备信息
        ScriptDevice device = new ScriptDevice();
        device.setDeviceAndroidId(loginRequest.getDeviceAndroidId());
        device.setDeviceWidth(loginRequest.getDeviceWidth());
        device.setDeviceHeight(loginRequest.getDeviceHeight());
        device.setDeviceBuildId(loginRequest.getDeviceBuildId());
        device.setDeviceBroad(loginRequest.getDeviceBroad());
        device.setDeviceBrand(loginRequest.getDeviceBrand());
        device.setDeviceName(loginRequest.getDeviceName());
        device.setDeviceModel(loginRequest.getDeviceModel());
        device.setDeviceSdkInt(loginRequest.getDeviceSdkInt());
        device.setDeviceIMEI(loginRequest.getDeviceIMEI());
        
        scriptDeviceService.saveOrUpdateDevice(device);

        // 6. 绑定卡密和设备（如果未绑定）
        boolean firstBinding = false;
        if (!isDeviceBound)
        {
            scriptCardDeviceService.bindCardToDevice(loginRequest.getCardNo(), loginRequest.getDeviceAndroidId(), "system");
            firstBinding = true;
        }

        // 7. 设置卡密过期时间（仅在首次绑定时设置）
        if (scriptCard.getExpireTime() == null && firstBinding)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, scriptCard.getExpireDay());
            scriptCard.setExpireTime(calendar.getTime());
            scriptCard.setUpdateBy("system");
            scriptCardService.updateScriptCard(scriptCard);
        }

        // 8. 获取关联的游戏列表
        List<ScriptCardGame> gameList = scriptCardGameService.selectGamesByCardNo(loginRequest.getCardNo());

        // 9. 创建脚本登录用户
        Set<String> permissions = new HashSet<>();
        // 根据游戏列表生成权限
        if (gameList != null && !gameList.isEmpty())
        {
            for (ScriptCardGame game : gameList)
            {
                permissions.add("script:game:" + game.getGameId());
            }
        }

        ScriptLoginUser scriptLoginUser = new ScriptLoginUser(scriptCard, loginRequest.getDeviceAndroidId(), permissions);
        scriptLoginUser.setGameList(gameList);
        scriptLoginUser.setFirstBinding(firstBinding);

        return scriptLoginUser;
    }

    /**
     * 创建登录令牌
     * 
     * @param scriptLoginUser 脚本登录用户
     * @return 令牌
     */
    @Override
    public String createToken(ScriptLoginUser scriptLoginUser)
    {
        String token = IdUtils.fastUUID();
        scriptLoginUser.setToken(token);
        setUserAgent(scriptLoginUser);
        refreshToken(scriptLoginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, scriptLoginUser.getUsername());
        return createToken(claims);
    }

    /**
     * 验证令牌有效性
     * 
     * @param scriptLoginUser 脚本登录用户
     */
    @Override
    public void verifyToken(ScriptLoginUser scriptLoginUser)
    {
        long expireTime = scriptLoginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(scriptLoginUser);
        }
    }

    /**
     * 刷新令牌
     * 
     * @param scriptLoginUser 脚本登录用户
     */
    @Override
    public void refreshToken(ScriptLoginUser scriptLoginUser)
    {
        scriptLoginUser.setLoginTime(System.currentTimeMillis());
        scriptLoginUser.setExpireTime(scriptLoginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将scriptLoginUser缓存
        String userKey = getTokenKey(scriptLoginUser.getToken());
        redisCache.setCacheObject(userKey, scriptLoginUser, expireTime, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 删除用户缓存记录
     * 
     * @param token 令牌
     */
    @Override
    public void logout(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 设置用户代理信息
     * 
     * @param scriptLoginUser 脚本登录用户
     */
    public void setUserAgent(ScriptLoginUser scriptLoginUser)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        scriptLoginUser.setIpaddr(ip);
        scriptLoginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        scriptLoginUser.setBrowser(userAgent.getBrowser().getName());
        scriptLoginUser.setOs(userAgent.getOperatingSystem().getName());
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

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}