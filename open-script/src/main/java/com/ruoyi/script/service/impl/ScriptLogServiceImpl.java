package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.script.domain.ScriptLog;
import com.ruoyi.script.mapper.ScriptLogMapper;
import com.ruoyi.script.service.IScriptLogService;

/**
 * 脚本日志信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptLogServiceImpl implements IScriptLogService
{
    @Autowired
    private ScriptLogMapper scriptLogMapper;

    /**
     * 查询脚本日志信息
     * 
     * @param id 脚本日志信息主键
     * @return 脚本日志信息
     */
    @Override
    public ScriptLog selectScriptLogById(Long id)
    {
        return scriptLogMapper.selectScriptLogById(id);
    }

    /**
     * 查询脚本日志信息列表
     * 
     * @param scriptLog 脚本日志信息
     * @return 脚本日志信息
     */
    @Override
    public List<ScriptLog> selectScriptLogList(ScriptLog scriptLog)
    {
        return scriptLogMapper.selectScriptLogList(scriptLog);
    }

    /**
     * 查询所有脚本日志信息
     * 
     * @return 脚本日志信息集合
     */
    @Override
    public List<ScriptLog> selectScriptLogAll()
    {
        return scriptLogMapper.selectScriptLogAll();
    }

    /**
     * 通过设备ID查询脚本日志列表
     * 
     * @param androidId 设备ID
     * @return 脚本日志列表
     */
    @Override
    public List<ScriptLog> selectScriptLogByAndroidId(String androidId)
    {
        return scriptLogMapper.selectScriptLogByAndroidId(androidId);
    }

    /**
     * 根据日志级别查询脚本日志列表
     * 
     * @param level 日志级别
     * @return 脚本日志列表
     */
    @Override
    public List<ScriptLog> selectScriptLogByLevel(String level)
    {
        return scriptLogMapper.selectScriptLogByLevel(level);
    }

    /**
     * 新增脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    @Override
    public int insertScriptLog(ScriptLog scriptLog)
    {
        return scriptLogMapper.insertScriptLog(scriptLog);
    }

    /**
     * 修改脚本日志信息
     * 
     * @param scriptLog 脚本日志信息
     * @return 结果
     */
    @Override
    public int updateScriptLog(ScriptLog scriptLog)
    {
        return scriptLogMapper.updateScriptLog(scriptLog);
    }

    /**
     * 批量删除脚本日志信息
     * 
     * @param ids 需要删除的脚本日志信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptLogByIds(Long[] ids)
    {
        return scriptLogMapper.deleteScriptLogByIds(ids);
    }

    /**
     * 删除脚本日志信息信息
     * 
     * @param id 脚本日志信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptLogById(Long id)
    {
        return scriptLogMapper.deleteScriptLogById(id);
    }

    /**
     * 清空指定设备的日志
     * 
     * @param androidId 设备ID
     * @return 结果
     */
    @Override
    public int deleteScriptLogByAndroidId(String androidId)
    {
        return scriptLogMapper.deleteScriptLogByAndroidId(androidId);
    }

    /**
     * 清空指定日期之前的日志
     * 
     * @param beforeDate 指定日期
     * @return 结果
     */
    @Override
    public int deleteScriptLogBeforeDate(String beforeDate)
    {
        return scriptLogMapper.deleteScriptLogBeforeDate(beforeDate);
    }
}