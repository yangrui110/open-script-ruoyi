package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.script.domain.ScriptCardDevice;
import com.ruoyi.script.mapper.ScriptCardDeviceMapper;
import com.ruoyi.script.service.IScriptCardDeviceService;

/**
 * 卡密关联设备信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptCardDeviceServiceImpl implements IScriptCardDeviceService
{
    @Autowired
    private ScriptCardDeviceMapper scriptCardDeviceMapper;

    /**
     * 查询卡密关联设备信息
     * 
     * @param id 卡密关联设备信息主键
     * @return 卡密关联设备信息
     */
    @Override
    public ScriptCardDevice selectScriptCardDeviceById(Long id)
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceById(id);
    }

    /**
     * 查询卡密关联设备信息列表
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 卡密关联设备信息
     */
    @Override
    public List<ScriptCardDevice> selectScriptCardDeviceList(ScriptCardDevice scriptCardDevice)
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceList(scriptCardDevice);
    }

    /**
     * 查询所有卡密关联设备信息
     * 
     * @return 卡密关联设备信息集合
     */
    @Override
    public List<ScriptCardDevice> selectScriptCardDeviceAll()
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceAll();
    }

    /**
     * 通过卡密查询关联的设备列表
     * 
     * @param cardNo 卡密
     * @return 卡密关联设备列表
     */
    @Override
    public List<ScriptCardDevice> selectScriptCardDeviceByCardNo(String cardNo)
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceByCardNo(cardNo);
    }

    /**
     * 通过Android ID查询关联的卡密列表
     * 
     * @param deviceAndroidId Android ID
     * @return 卡密关联设备列表
     */
    @Override
    public List<ScriptCardDevice> selectScriptCardDeviceByAndroidId(String deviceAndroidId)
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceByAndroidId(deviceAndroidId);
    }

    /**
     * 检查卡密和设备是否已经关联
     * 
     * @param cardNo 卡密
     * @param deviceAndroidId Android ID
     * @return 卡密关联设备信息
     */
    @Override
    public ScriptCardDevice selectScriptCardDeviceByCardNoAndAndroidId(String cardNo, String deviceAndroidId)
    {
        return scriptCardDeviceMapper.selectScriptCardDeviceByCardNoAndAndroidId(cardNo, deviceAndroidId);
    }

    /**
     * 新增卡密关联设备信息
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 结果
     */
    @Override
    public int insertScriptCardDevice(ScriptCardDevice scriptCardDevice)
    {
        return scriptCardDeviceMapper.insertScriptCardDevice(scriptCardDevice);
    }

    /**
     * 修改卡密关联设备信息
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 结果
     */
    @Override
    public int updateScriptCardDevice(ScriptCardDevice scriptCardDevice)
    {
        return scriptCardDeviceMapper.updateScriptCardDevice(scriptCardDevice);
    }

    /**
     * 批量删除卡密关联设备信息
     * 
     * @param ids 需要删除的卡密关联设备信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptCardDeviceByIds(Long[] ids)
    {
        return scriptCardDeviceMapper.deleteScriptCardDeviceByIds(ids);
    }

    /**
     * 删除卡密关联设备信息信息
     * 
     * @param id 卡密关联设备信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptCardDeviceById(Long id)
    {
        return scriptCardDeviceMapper.deleteScriptCardDeviceById(id);
    }

    /**
     * 删除指定卡密的所有设备关联
     * 
     * @param cardNo 卡密
     * @return 结果
     */
    @Override
    public int deleteScriptCardDeviceByCardNo(String cardNo)
    {
        return scriptCardDeviceMapper.deleteScriptCardDeviceByCardNo(cardNo);
    }

    /**
     * 统计指定卡密已关联的设备数量
     * 
     * @param cardNo 卡密
     * @return 设备数量
     */
    @Override
    public int countDevicesByCardNo(String cardNo)
    {
        return scriptCardDeviceMapper.countDevicesByCardNo(cardNo);
    }

    /**
     * 绑定卡密和设备（如果未绑定的话）
     * 
     * @param cardNo 卡密
     * @param deviceAndroidId Android ID
     * @param createBy 创建者
     * @return 结果
     */
    @Override
    public int bindCardToDevice(String cardNo, String deviceAndroidId, String createBy)
    {
        // 检查是否已经绑定
        ScriptCardDevice existing = scriptCardDeviceMapper.selectScriptCardDeviceByCardNoAndAndroidId(cardNo, deviceAndroidId);
        if (existing != null)
        {
            return 1; // 已经绑定，返回成功
        }

        // 创建新的绑定关系
        ScriptCardDevice scriptCardDevice = new ScriptCardDevice();
        scriptCardDevice.setCardNo(cardNo);
        scriptCardDevice.setDeviceAndroidId(deviceAndroidId);
        scriptCardDevice.setCreateBy(createBy);
        scriptCardDevice.setDelFlag("0");
        
        return scriptCardDeviceMapper.insertScriptCardDevice(scriptCardDevice);
    }
}