package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptCardGame;

/**
 * 卡号关联游戏Service接口
 * 
 * @author ruoyi
 */
public interface IScriptCardGameService
{
    /**
     * 查询卡号关联游戏信息
     * 
     * @param id 卡号关联游戏信息主键
     * @return 卡号关联游戏信息
     */
    public ScriptCardGame selectScriptCardGameById(Long id);

    /**
     * 查询卡号关联游戏信息列表
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 卡号关联游戏信息集合
     */
    public List<ScriptCardGame> selectScriptCardGameList(ScriptCardGame scriptCardGame);

    /**
     * 通过卡号查询关联的游戏列表
     * 
     * @param cardNo 卡号
     * @return 关联的游戏列表
     */
    public List<ScriptCardGame> selectGamesByCardNo(String cardNo);

    /**
     * 通过游戏ID查询关联的卡号列表
     * 
     * @param gameId 游戏ID
     * @return 关联的卡号列表
     */
    public List<ScriptCardGame> selectCardsByGameId(Long gameId);

    /**
     * 新增卡号关联游戏信息
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 结果
     */
    public int insertScriptCardGame(ScriptCardGame scriptCardGame);

    /**
     * 批量新增卡号关联游戏信息
     * 
     * @param scriptCardGameList 卡号关联游戏信息列表
     * @return 结果
     */
    public int batchInsertScriptCardGame(List<ScriptCardGame> scriptCardGameList);

    /**
     * 修改卡号关联游戏信息
     * 
     * @param scriptCardGame 卡号关联游戏信息
     * @return 结果
     */
    public int updateScriptCardGame(ScriptCardGame scriptCardGame);

    /**
     * 批量删除卡号关联游戏信息
     * 
     * @param ids 需要删除的卡号关联游戏信息主键集合
     * @return 结果
     */
    public int deleteScriptCardGameByIds(Long[] ids);

    /**
     * 删除卡号关联游戏信息信息
     * 
     * @param id 卡号关联游戏信息主键
     * @return 结果
     */
    public int deleteScriptCardGameById(Long id);

    /**
     * 根据卡号删除关联游戏
     * 
     * @param cardNo 卡号
     * @return 结果
     */
    public int deleteScriptCardGameByCardNo(String cardNo);

    /**
     * 根据游戏ID删除关联卡号
     * 
     * @param gameId 游戏ID
     * @return 结果
     */
    public int deleteScriptCardGameByGameId(Long gameId);

    /**
     * 保存卡号关联的游戏（先删除原有关联，再批量插入新关联）
     * 
     * @param cardNo 卡号
     * @param gameIds 游戏ID列表
     * @param createBy 创建者
     * @return 结果
     */
    public int saveCardGameRelations(String cardNo, Long[] gameIds, String createBy);
}