package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptCardDevice;

/**
 * 卡密关联设备信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptCardDeviceService
{
    /**
     * 查询卡密关联设备信息
     * 
     * @param id 卡密关联设备信息主键
     * @return 卡密关联设备信息
     */
    public ScriptCardDevice selectScriptCardDeviceById(Long id);

    /**
     * 查询卡密关联设备信息列表
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 卡密关联设备信息集合
     */
    public List<ScriptCardDevice> selectScriptCardDeviceList(ScriptCardDevice scriptCardDevice);

    /**
     * 查询所有卡密关联设备信息
     * 
     * @return 卡密关联设备信息集合
     */
    public List<ScriptCardDevice> selectScriptCardDeviceAll();

    /**
     * 通过卡密查询关联的设备列表
     * 
     * @param cardNo 卡密
     * @return 卡密关联设备列表
     */
    public List<ScriptCardDevice> selectScriptCardDeviceByCardNo(String cardNo);

    /**
     * 通过Android ID查询关联的卡密列表
     * 
     * @param deviceAndroidId Android ID
     * @return 卡密关联设备列表
     */
    public List<ScriptCardDevice> selectScriptCardDeviceByAndroidId(String deviceAndroidId);

    /**
     * 检查卡密和设备是否已经关联
     * 
     * @param cardNo 卡密
     * @param deviceAndroidId Android ID
     * @return 卡密关联设备信息
     */
    public ScriptCardDevice selectScriptCardDeviceByCardNoAndAndroidId(String cardNo, String deviceAndroidId);

    /**
     * 新增卡密关联设备信息
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 结果
     */
    public int insertScriptCardDevice(ScriptCardDevice scriptCardDevice);

    /**
     * 修改卡密关联设备信息
     * 
     * @param scriptCardDevice 卡密关联设备信息
     * @return 结果
     */
    public int updateScriptCardDevice(ScriptCardDevice scriptCardDevice);

    /**
     * 批量删除卡密关联设备信息
     * 
     * @param ids 需要删除的卡密关联设备信息主键集合
     * @return 结果
     */
    public int deleteScriptCardDeviceByIds(Long[] ids);

    /**
     * 删除卡密关联设备信息信息
     * 
     * @param id 卡密关联设备信息主键
     * @return 结果
     */
    public int deleteScriptCardDeviceById(Long id);

    /**
     * 删除指定卡密的所有设备关联
     * 
     * @param cardNo 卡密
     * @return 结果
     */
    public int deleteScriptCardDeviceByCardNo(String cardNo);

    /**
     * 统计指定卡密已关联的设备数量
     * 
     * @param cardNo 卡密
     * @return 设备数量
     */
    public int countDevicesByCardNo(String cardNo);

    /**
     * 绑定卡密和设备（如果未绑定的话）
     * 
     * @param cardNo 卡密
     * @param deviceAndroidId Android ID
     * @param createBy 创建者
     * @return 结果
     */
    public int bindCardToDevice(String cardNo, String deviceAndroidId, String createBy);
}