package com.ruoyi.script.mapper;

import java.util.List;
import com.ruoyi.script.domain.ScriptCard;

/**
 * 卡密信息 数据层
 * 
 * @author ruoyi
 */
public interface ScriptCardMapper
{
    /**
     * 查询卡密数据集合
     * 
     * @param scriptCard 卡密信息
     * @return 卡密数据集合
     */
    public List<ScriptCard> selectScriptCardList(ScriptCard scriptCard);

    /**
     * 查询所有卡密
     * 
     * @return 卡密列表
     */
    public List<ScriptCard> selectScriptCardAll();

    /**
     * 通过卡密ID查询卡密信息
     * 
     * @param id 卡密ID
     * @return 卡密对象信息
     */
    public ScriptCard selectScriptCardById(Long id);

    /**
     * 通过卡密号查询卡密信息
     * 
     * @param cardNo 卡密号
     * @return 卡密对象信息
     */
    public ScriptCard selectScriptCardByCardNo(String cardNo);

    /**
     * 删除卡密信息
     * 
     * @param id 卡密ID
     * @return 结果
     */
    public int deleteScriptCardById(Long id);

    /**
     * 批量删除卡密信息
     * 
     * @param ids 需要删除的卡密ID
     * @return 结果
     */
    public int deleteScriptCardByIds(Long[] ids);

    /**
     * 修改卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    public int updateScriptCard(ScriptCard scriptCard);

    /**
     * 新增卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    public int insertScriptCard(ScriptCard scriptCard);

    /**
     * 校验卡密号唯一性
     * 
     * @param cardNo 卡密号
     * @return 结果
     */
    public ScriptCard checkCardNoUnique(String cardNo);
}