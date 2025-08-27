package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptGame;

/**
 * 游戏信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptGameService
{
    /**
     * 查询游戏信息
     * 
     * @param id 游戏信息主键
     * @return 游戏信息
     */
    public ScriptGame selectScriptGameById(Long id);

    /**
     * 查询游戏信息列表
     * 
     * @param scriptGame 游戏信息
     * @return 游戏信息集合
     */
    public List<ScriptGame> selectScriptGameList(ScriptGame scriptGame);

    /**
     * 查询所有游戏信息
     * 
     * @return 游戏信息集合
     */
    public List<ScriptGame> selectScriptGameAll();

    /**
     * 新增游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    public int insertScriptGame(ScriptGame scriptGame);

    /**
     * 修改游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    public int updateScriptGame(ScriptGame scriptGame);

    /**
     * 批量删除游戏信息
     * 
     * @param ids 需要删除的游戏信息主键集合
     * @return 结果
     */
    public int deleteScriptGameByIds(Long[] ids);

    /**
     * 删除游戏信息信息
     * 
     * @param id 游戏信息主键
     * @return 结果
     */
    public int deleteScriptGameById(Long id);

    /**
     * 校验游戏名称是否唯一
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    public boolean checkTitleUnique(ScriptGame scriptGame);
}