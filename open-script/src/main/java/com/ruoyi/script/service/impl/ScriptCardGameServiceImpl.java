package com.ruoyi.script.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.script.domain.ScriptCardGame;
import com.ruoyi.script.mapper.ScriptCardGameMapper;
import com.ruoyi.script.service.IScriptCardGameService;

/**
 * 卡号关联游戏 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptCardGameServiceImpl implements IScriptCardGameService
{
    @Autowired
    private ScriptCardGameMapper scriptCardGameMapper;

    /**
     * 查询卡号关联游戏信息
     * 
     * @param id 卡号关联游戏信息主键
     * @return 卡号关联游戏信息
     */
    @Override
    public ScriptCardGame selectScriptCardGameById(Long id)
    {
        return scriptCardGameMapper.selectScriptCardGameById(id);
    }

    /**
     * 查询卡号关联游戏信息列表
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 卡号关联游戏信息
     */
    @Override
    public List<ScriptCardGame> selectScriptCardGameList(ScriptCardGame scriptCardGame)
    {
        return scriptCardGameMapper.selectScriptCardGameList(scriptCardGame);
    }

    /**
     * 通过卡号查询关联的游戏列表
     * 
     * @param cardNo 卡号
     * @return 关联的游戏列表
     */
    @Override
    public List<ScriptCardGame> selectGamesByCardNo(String cardNo)
    {
        return scriptCardGameMapper.selectGamesByCardNo(cardNo);
    }

    /**
     * 通过游戏ID查询关联的卡号列表
     * 
     * @param gameId 游戏ID
     * @return 关联的卡号列表
     */
    @Override
    public List<ScriptCardGame> selectCardsByGameId(Long gameId)
    {
        return scriptCardGameMapper.selectCardsByGameId(gameId);
    }

    /**
     * 新增卡号关联游戏信息
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 结果
     */
    @Override
    public int insertScriptCardGame(ScriptCardGame scriptCardGame)
    {
        return scriptCardGameMapper.insertScriptCardGame(scriptCardGame);
    }

    /**
     * 批量新增卡号关联游戏信息
     * 
     * @param scriptCardGameList 卡号关联游戏信息列表
     * @return 结果
     */
    @Override
    public int batchInsertScriptCardGame(List<ScriptCardGame> scriptCardGameList)
    {
        if (scriptCardGameList == null || scriptCardGameList.isEmpty())
        {
            return 0;
        }
        return scriptCardGameMapper.batchInsertScriptCardGame(scriptCardGameList);
    }

    /**
     * 修改卡号关联游戏信息
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 结果
     */
    @Override
    public int updateScriptCardGame(ScriptCardGame scriptCardGame)
    {
        return scriptCardGameMapper.updateScriptCardGame(scriptCardGame);
    }

    /**
     * 批量删除卡号关联游戏信息
     * 
     * @param ids 需要删除的卡号关联游戏信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptCardGameByIds(Long[] ids)
    {
        return scriptCardGameMapper.deleteScriptCardGameByIds(ids);
    }

    /**
     * 删除卡号关联游戏信息信息
     * 
     * @param id 卡号关联游戏信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptCardGameById(Long id)
    {
        return scriptCardGameMapper.deleteScriptCardGameById(id);
    }

    /**
     * 根据卡号删除关联游戏
     * 
     * @param cardNo 卡号
     * @return 结果
     */
    @Override
    public int deleteScriptCardGameByCardNo(String cardNo)
    {
        return scriptCardGameMapper.deleteScriptCardGameByCardNo(cardNo);
    }

    /**
     * 根据游戏ID删除关联卡号
     * 
     * @param gameId 游戏ID
     * @return 结果
     */
    @Override
    public int deleteScriptCardGameByGameId(Long gameId)
    {
        return scriptCardGameMapper.deleteScriptCardGameByGameId(gameId);
    }

    /**
     * 保存卡号关联的游戏（先删除原有关联，再批量插入新关联）
     * 
     * @param cardNo 卡号
     * @param gameIds 游戏ID列表
     * @param createBy 创建者
     * @return 结果
     */
    @Override
    @Transactional
    public int saveCardGameRelations(String cardNo, Long[] gameIds, String createBy)
    {
        // 先删除原有关联
        scriptCardGameMapper.deleteScriptCardGameByCardNo(cardNo);
        
        // 如果没有选择游戏，直接返回
        if (gameIds == null || gameIds.length == 0)
        {
            return 1;
        }
        
        // 批量插入新关联
        List<ScriptCardGame> scriptCardGameList = new ArrayList<>();
        for (Long gameId : gameIds)
        {
            ScriptCardGame scriptCardGame = new ScriptCardGame();
            scriptCardGame.setCardNo(cardNo);
            scriptCardGame.setGameId(gameId);
            scriptCardGame.setCreateBy(createBy);
            scriptCardGameList.add(scriptCardGame);
        }
        
        return batchInsertScriptCardGame(scriptCardGameList);
    }
}