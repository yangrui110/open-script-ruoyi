package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptDevice;

/**
 * 设备信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptDeviceService
{
    /**
     * 查询设备信息
     * 
     * @param id 设备信息主键
     * @return 设备信息
     */
    public ScriptDevice selectScriptDeviceById(Long id);

    /**
     * 查询设备信息列表
     * 
     * @param scriptDevice 设备信息
     * @return 设备信息集合
     */
    public List<ScriptDevice> selectScriptDeviceList(ScriptDevice scriptDevice);

    /**
     * 查询所有设备信息
     * 
     * @return 设备信息集合
     */
    public List<ScriptDevice> selectScriptDeviceAll();

    /**
     * 通过Android ID查询设备信息
     * 
     * @param deviceAndroidId Android ID
     * @return 设备信息
     */
    public ScriptDevice selectScriptDeviceByAndroidId(String deviceAndroidId);

    /**
     * 新增设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    public int insertScriptDevice(ScriptDevice scriptDevice);

    /**
     * 修改设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    public int updateScriptDevice(ScriptDevice scriptDevice);

    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的设备信息主键集合
     * @return 结果
     */
    public int deleteScriptDeviceByIds(Long[] ids);

    /**
     * 删除设备信息信息
     * 
     * @param id 设备信息主键
     * @return 结果
     */
    public int deleteScriptDeviceById(Long id);

    /**
     * 保存或更新设备信息（用于登录时的设备信息同步）
     * 
     * @param scriptDevice 设备信息
     * @return 设备信息
     */
    public ScriptDevice saveOrUpdateDevice(ScriptDevice scriptDevice);
}