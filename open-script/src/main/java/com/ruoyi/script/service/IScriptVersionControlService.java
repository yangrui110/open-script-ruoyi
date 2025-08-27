package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptVersionControl;
import com.ruoyi.script.domain.vo.ScriptVersionControlVo;

/**
 * 脚本版本信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptVersionControlService
{
    /**
     * 查询脚本版本信息
     * 
     * @param id 脚本版本信息主键
     * @return 脚本版本信息
     */
    public ScriptVersionControl selectScriptVersionControlById(Long id);

    /**
     * 查询脚本版本信息列表
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 脚本版本信息集合
     */
    public List<ScriptVersionControl> selectScriptVersionControlList(ScriptVersionControl scriptVersionControl);

    /**
     * 查询所有脚本版本信息
     * 
     * @return 脚本版本信息集合
     */
    public List<ScriptVersionControl> selectScriptVersionControlAll();

    /**
     * 通过游戏ID查询脚本版本列表
     * 
     * @param gameId 游戏ID
     * @return 脚本版本列表
     */
    public List<ScriptVersionControl> selectScriptVersionControlByGameId(Long gameId);

    /**
     * 获取指定游戏的最新版本号
     * 
     * @param gameId 游戏ID
     * @return 最新版本号
     */
    public Integer selectMaxVersionByGameId(Long gameId);

    /**
     * 新增脚本版本信息（自动生成版本号）
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    public int insertScriptVersionControl(ScriptVersionControl scriptVersionControl);

    /**
     * 修改脚本版本信息
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    public int updateScriptVersionControl(ScriptVersionControl scriptVersionControl);

    /**
     * 批量删除脚本版本信息
     * 
     * @param ids 需要删除的脚本版本信息主键集合
     * @return 结果
     */
    public int deleteScriptVersionControlByIds(Long[] ids);

    /**
     * 删除脚本版本信息信息
     * 
     * @param id 脚本版本信息主键
     * @return 结果
     */
    public int deleteScriptVersionControlById(Long id);

    /**
     * 获取指定游戏的最新版本信息
     * 
     * @param gameId 游戏ID
     * @return 最新版本信息
     */
    public ScriptVersionControlVo getLatestVersion(Long gameId);
}