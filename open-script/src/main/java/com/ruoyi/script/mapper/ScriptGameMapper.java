package com.ruoyi.script.mapper;

import java.util.List;
import com.ruoyi.script.domain.ScriptGame;

/**
 * 游戏信息 数据层
 * 
 * @author ruoyi
 */
public interface ScriptGameMapper
{
    /**
     * 查询游戏数据集合
     * 
     * @param scriptGame 游戏信息
     * @return 游戏数据集合
     */
    public List<ScriptGame> selectScriptGameList(ScriptGame scriptGame);

    /**
     * 查询所有游戏
     * 
     * @return 游戏列表
     */
    public List<ScriptGame> selectScriptGameAll();

    /**
     * 通过游戏ID查询游戏信息
     * 
     * @param id 游戏ID
     * @return 游戏对象信息
     */
    public ScriptGame selectScriptGameById(Long id);

    /**
     * 删除游戏信息
     * 
     * @param id 游戏ID
     * @return 结果
     */
    public int deleteScriptGameById(Long id);

    /**
     * 批量删除游戏信息
     * 
     * @param ids 需要删除的游戏ID
     * @return 结果
     */
    public int deleteScriptGameByIds(Long[] ids);

    /**
     * 修改游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    public int updateScriptGame(ScriptGame scriptGame);

    /**
     * 新增游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    public int insertScriptGame(ScriptGame scriptGame);

    /**
     * 校验游戏名称唯一性
     * 
     * @param title 游戏名称
     * @return 结果
     */
    public ScriptGame checkTitleUnique(String title);
}