package com.ruoyi.script.mapper;

import java.util.List;
import com.ruoyi.script.domain.ScriptVersionControl;
import org.apache.ibatis.annotations.Param;

/**
 * 脚本版本信息 数据层
 * 
 * @author ruoyi
 */
public interface ScriptVersionControlMapper
{
    /**
     * 查询脚本版本数据集合
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 脚本版本数据集合
     */
    public List<ScriptVersionControl> selectScriptVersionControlList(ScriptVersionControl scriptVersionControl);

    /**
     * 查询所有脚本版本
     * 
     * @return 脚本版本列表
     */
    public List<ScriptVersionControl> selectScriptVersionControlAll();

    /**
     * 通过脚本版本ID查询脚本版本信息
     * 
     * @param id 脚本版本ID
     * @return 脚本版本对象信息
     */
    public ScriptVersionControl selectScriptVersionControlById(Long id);

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
     * 根据游戏ID和版本号查询版本信息
     * 
     * @param gameId 游戏ID
     * @param version 版本号
     * @return 版本信息
     */
    public ScriptVersionControl selectLatestVersionByGameId(@Param("gameId") Long gameId,@Param("version") Integer version);

    /**
     * 删除脚本版本信息
     * 
     * @param id 脚本版本ID
     * @return 结果
     */
    public int deleteScriptVersionControlById(Long id);

    /**
     * 批量删除脚本版本信息
     * 
     * @param ids 需要删除的脚本版本ID
     * @return 结果
     */
    public int deleteScriptVersionControlByIds(Long[] ids);

    /**
     * 修改脚本版本信息
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    public int updateScriptVersionControl(ScriptVersionControl scriptVersionControl);

    /**
     * 新增脚本版本信息
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    public int insertScriptVersionControl(ScriptVersionControl scriptVersionControl);
}