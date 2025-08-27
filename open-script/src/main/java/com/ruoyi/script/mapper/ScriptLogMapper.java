package com.ruoyi.script.mapper;

import java.util.List;
import com.ruoyi.script.domain.ScriptLog;

/**
 * 脚本日志信息 数据层
 * 
 * @author ruoyi
 */
public interface ScriptLogMapper
{
    /**
     * 查询脚本日志数据集合
     * 
     * @param scriptLog 脚本日志信息
     * @return 脚本日志数据集合
     */
    public List<ScriptLog> selectScriptLogList(ScriptLog scriptLog);

    /**
     * 查询所有脚本日志
     * 
     * @return 脚本日志列表
     */
    public List<ScriptLog> selectScriptLogAll();

    /**
     * 通过脚本日志ID查询脚本日志信息
     * 
     * @param id 脚本日志ID
     * @return 脚本日志对象信息
     */
    public ScriptLog selectScriptLogById(Long id);

    /**
     * 通过设备ID查询脚本日志列表
     * 
     * @param androidId 设备ID
     * @return 脚本日志列表
     */
    public List<ScriptLog> selectScriptLogByAndroidId(String androidId);

    /**
     * 根据日志级别查询脚本日志列表
     * 
     * @param level 日志级别
     * @return 脚本日志列表
     */
    public List<ScriptLog> selectScriptLogByLevel(String level);

    /**
     * 删除脚本日志信息
     * 
     * @param id 脚本日志ID
     * @return 结果
     */
    public int deleteScriptLogById(Long id);

    /**
     * 批量删除脚本日志信息
     * 
     * @param ids 需要删除的脚本日志ID
     * @return 结果
     */
    public int deleteScriptLogByIds(Long[] ids);

    /**
     * 修改脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    public int updateScriptLog(ScriptLog scriptLog);

    /**
     * 新增脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    public int insertScriptLog(ScriptLog scriptLog);

    /**
     * 清空指定设备的日志
     * 
     * @param androidId 设备ID
     * @return 结果
     */
    public int deleteScriptLogByAndroidId(String androidId);

    /**
     * 清空指定日期之前的日志
     * 
     * @param beforeDate 指定日期
     * @return 结果
     */
    public int deleteScriptLogBeforeDate(String beforeDate);
}