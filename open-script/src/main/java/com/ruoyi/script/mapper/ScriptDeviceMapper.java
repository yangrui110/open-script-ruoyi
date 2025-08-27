package com.ruoyi.script.mapper;

import java.util.List;
import com.ruoyi.script.domain.ScriptDevice;

/**
 * 设备信息 数据层
 * 
 * @author ruoyi
 */
public interface ScriptDeviceMapper
{
    /**
     * 查询设备数据集合
     * 
     * @param scriptDevice 设备信息
     * @return 设备数据集合
     */
    public List<ScriptDevice> selectScriptDeviceList(ScriptDevice scriptDevice);

    /**
     * 查询所有设备
     * 
     * @return 设备列表
     */
    public List<ScriptDevice> selectScriptDeviceAll();

    /**
     * 通过设备ID查询设备信息
     * 
     * @param id 设备ID
     * @return 设备对象信息
     */
    public ScriptDevice selectScriptDeviceById(Long id);

    /**
     * 通过Android ID查询设备信息
     * 
     * @param deviceAndroidId Android ID
     * @return 设备对象信息
     */
    public ScriptDevice selectScriptDeviceByAndroidId(String deviceAndroidId);

    /**
     * 删除设备信息
     * 
     * @param id 设备ID
     * @return 结果
     */
    public int deleteScriptDeviceById(Long id);

    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的设备ID
     * @return 结果
     */
    public int deleteScriptDeviceByIds(Long[] ids);

    /**
     * 修改设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    public int updateScriptDevice(ScriptDevice scriptDevice);

    /**
     * 新增设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    public int insertScriptDevice(ScriptDevice scriptDevice);
}