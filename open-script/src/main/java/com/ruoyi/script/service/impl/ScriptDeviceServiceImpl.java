package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.script.domain.ScriptDevice;
import com.ruoyi.script.mapper.ScriptDeviceMapper;
import com.ruoyi.script.service.IScriptDeviceService;

/**
 * 设备信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptDeviceServiceImpl implements IScriptDeviceService
{
    @Autowired
    private ScriptDeviceMapper scriptDeviceMapper;

    /**
     * 查询设备信息
     * 
     * @param id 设备信息主键
     * @return 设备信息
     */
    @Override
    public ScriptDevice selectScriptDeviceById(Long id)
    {
        return scriptDeviceMapper.selectScriptDeviceById(id);
    }

    /**
     * 查询设备信息列表
     * 
     * @param scriptDevice 设备信息
     * @return 设备信息
     */
    @Override
    public List<ScriptDevice> selectScriptDeviceList(ScriptDevice scriptDevice)
    {
        return scriptDeviceMapper.selectScriptDeviceList(scriptDevice);
    }

    /**
     * 查询所有设备信息
     * 
     * @return 设备信息集合
     */
    @Override
    public List<ScriptDevice> selectScriptDeviceAll()
    {
        return scriptDeviceMapper.selectScriptDeviceAll();
    }

    /**
     * 通过Android ID查询设备信息
     * 
     * @param deviceAndroidId Android ID
     * @return 设备信息
     */
    @Override
    public ScriptDevice selectScriptDeviceByAndroidId(String deviceAndroidId)
    {
        return scriptDeviceMapper.selectScriptDeviceByAndroidId(deviceAndroidId);
    }

    /**
     * 新增设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    @Override
    public int insertScriptDevice(ScriptDevice scriptDevice)
    {
        return scriptDeviceMapper.insertScriptDevice(scriptDevice);
    }

    /**
     * 修改设备信息
     * 
     * @param scriptDevice 设备信息
     * @return 结果
     */
    @Override
    public int updateScriptDevice(ScriptDevice scriptDevice)
    {
        return scriptDeviceMapper.updateScriptDevice(scriptDevice);
    }

    /**
     * 批量删除设备信息
     * 
     * @param ids 需要删除的设备信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptDeviceByIds(Long[] ids)
    {
        return scriptDeviceMapper.deleteScriptDeviceByIds(ids);
    }

    /**
     * 删除设备信息信息
     * 
     * @param id 设备信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptDeviceById(Long id)
    {
        return scriptDeviceMapper.deleteScriptDeviceById(id);
    }

    /**
     * 保存或更新设备信息（用于登录时的设备信息同步）
     * 
     * @param scriptDevice 设备信息
     * @return 设备信息
     */
    @Override
    public ScriptDevice saveOrUpdateDevice(ScriptDevice scriptDevice)
    {
        if (StringUtils.isEmpty(scriptDevice.getDeviceAndroidId()))
        {
            throw new RuntimeException("设备Android ID不能为空");
        }

        // 查询设备是否已存在
        ScriptDevice existingDevice = scriptDeviceMapper.selectScriptDeviceByAndroidId(scriptDevice.getDeviceAndroidId());
        
        if (existingDevice != null)
        {
            // 设备已存在，更新设备信息
            scriptDevice.setId(existingDevice.getId());
            scriptDevice.setUpdateBy("system");
            scriptDeviceMapper.updateScriptDevice(scriptDevice);
            return scriptDevice;
        }
        else
        {
            // 设备不存在，新增设备信息
            scriptDevice.setCreateBy("system");
            scriptDevice.setDelFlag("0");
            scriptDeviceMapper.insertScriptDevice(scriptDevice);
            return scriptDevice;
        }
    }
}