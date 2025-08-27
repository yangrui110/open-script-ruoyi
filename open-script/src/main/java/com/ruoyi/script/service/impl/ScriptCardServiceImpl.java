package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.script.domain.ScriptCard;
import com.ruoyi.script.domain.ScriptCardGame;
import com.ruoyi.script.mapper.ScriptCardMapper;
import com.ruoyi.script.service.IScriptCardService;
import com.ruoyi.script.service.IScriptCardGameService;

/**
 * 卡密信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptCardServiceImpl implements IScriptCardService
{
    @Autowired
    private ScriptCardMapper scriptCardMapper;

    @Autowired
    private IScriptCardGameService scriptCardGameService;

    /**
     * 查询卡密信息
     * 
     * @param id 卡密信息主键
     * @return 卡密信息
     */
    @Override
    public ScriptCard selectScriptCardById(Long id)
    {
        ScriptCard scriptCard = scriptCardMapper.selectScriptCardById(id);
        if (scriptCard != null && StringUtils.isNotEmpty(scriptCard.getCardNo()))
        {
            // 查询关联的游戏列表
            List<ScriptCardGame> gameList = scriptCardGameService.selectGamesByCardNo(scriptCard.getCardNo());
            scriptCard.setGameList(gameList);
        }
        return scriptCard;
    }

    /**
     * 查询卡密信息列表
     * 
     * @param scriptCard 卡密信息
     * @return 卡密信息
     */
    @Override
    public List<ScriptCard> selectScriptCardList(ScriptCard scriptCard)
    {
        return scriptCardMapper.selectScriptCardList(scriptCard);
    }

    /**
     * 查询所有卡密信息
     * 
     * @return 卡密信息集合
     */
    @Override
    public List<ScriptCard> selectScriptCardAll()
    {
        return scriptCardMapper.selectScriptCardAll();
    }

    /**
     * 新增卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertScriptCard(ScriptCard scriptCard)
    {
        int result = scriptCardMapper.insertScriptCard(scriptCard);
        // 保存卡密关联的游戏
        if (result > 0 && scriptCard.getGameIds() != null && scriptCard.getGameIds().length > 0)
        {
            scriptCardGameService.saveCardGameRelations(scriptCard.getCardNo(), scriptCard.getGameIds(), scriptCard.getCreateBy());
        }
        return result;
    }

    /**
     * 修改卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateScriptCard(ScriptCard scriptCard)
    {
        int result = scriptCardMapper.updateScriptCard(scriptCard);
        // 保存卡密关联的游戏
        if (result > 0 && scriptCard.getGameIds() != null)
        {
            scriptCardGameService.saveCardGameRelations(scriptCard.getCardNo(), scriptCard.getGameIds(), scriptCard.getUpdateBy());
        }
        return result;
    }

    /**
     * 批量删除卡密信息
     * 
     * @param ids 需要删除的卡密信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteScriptCardByIds(Long[] ids)
    {
        // 先查询要删除的卡密信息，获取卡号列表
        for (Long id : ids)
        {
            ScriptCard scriptCard = scriptCardMapper.selectScriptCardById(id);
            if (scriptCard != null && StringUtils.isNotEmpty(scriptCard.getCardNo()))
            {
                // 删除卡密关联的游戏
                scriptCardGameService.deleteScriptCardGameByCardNo(scriptCard.getCardNo());
            }
        }
        return scriptCardMapper.deleteScriptCardByIds(ids);
    }

    /**
     * 删除卡密信息信息
     * 
     * @param id 卡密信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteScriptCardById(Long id)
    {
        // 先查询卡密信息，获取卡号
        ScriptCard scriptCard = scriptCardMapper.selectScriptCardById(id);
        if (scriptCard != null && StringUtils.isNotEmpty(scriptCard.getCardNo()))
        {
            // 删除卡密关联的游戏
            scriptCardGameService.deleteScriptCardGameByCardNo(scriptCard.getCardNo());
        }
        return scriptCardMapper.deleteScriptCardById(id);
    }

    /**
     * 校验卡密号是否唯一
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    @Override
    public boolean checkCardNoUnique(ScriptCard scriptCard)
    {
        Long id = StringUtils.isNull(scriptCard.getId()) ? -1L : scriptCard.getId();
        ScriptCard info = scriptCardMapper.checkCardNoUnique(scriptCard.getCardNo());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 通过卡密号查询卡密信息
     * 
     * @param cardNo 卡密号
     * @return 卡密信息
     */
    @Override
    public ScriptCard selectScriptCardByCardNo(String cardNo)
    {
        return scriptCardMapper.selectScriptCardByCardNo(cardNo);
    }
}