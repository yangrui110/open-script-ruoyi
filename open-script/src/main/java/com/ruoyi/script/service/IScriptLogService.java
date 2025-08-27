package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptLog;

/**
 * 脚本日志信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptLogService
{
    /**
     * 查询脚本日志信息
     * 
     * @param id 脚本日志信息主键
     * @return 脚本日志信息
     */
    public ScriptLog selectScriptLogById(Long id);

    /**
     * 查询脚本日志信息列表
     * 
     * @param scriptLog 脚本日志信息
     * @return 脚本日志信息集合
     */
    public List<ScriptLog> selectScriptLogList(ScriptLog scriptLog);

    /**
     * 查询所有脚本日志信息
     * 
     * @return 脚本日志信息集合
     */
    public List<ScriptLog> selectScriptLogAll();

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
     * 新增脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    public int insertScriptLog(ScriptLog scriptLog);

    /**
     * 修改脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    public int updateScriptLog(ScriptLog scriptLog);

    /**
     * 批量删除脚本日志信息
     * 
     * @param ids 需要删除的脚本日志信息主键集合
     * @return 结果
     */
    public int deleteScriptLogByIds(Long[] ids);

    /**
     * 删除脚本日志信息信息
     * 
     * @param id 脚本日志信息主键
     * @return 结果
     */
    public int deleteScriptLogById(Long id);

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