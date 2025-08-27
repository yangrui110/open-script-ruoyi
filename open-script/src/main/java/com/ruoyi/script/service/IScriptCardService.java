package com.ruoyi.script.service;

import java.util.List;
import com.ruoyi.script.domain.ScriptCard;

/**
 * 卡密信息Service接口
 * 
 * @author ruoyi
 */
public interface IScriptCardService
{
    /**
     * 查询卡密信息
     * 
     * @param id 卡密信息主键
     * @return 卡密信息
     */
    public ScriptCard selectScriptCardById(Long id);

    /**
     * 查询卡密信息列表
     * 
     * @param scriptCard 卡密信息
     * @return 卡密信息集合
     */
    public List<ScriptCard> selectScriptCardList(ScriptCard scriptCard);

    /**
     * 查询所有卡密信息
     * 
     * @return 卡密信息集合
     */
    public List<ScriptCard> selectScriptCardAll();

    /**
     * 新增卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    public int insertScriptCard(ScriptCard scriptCard);

    /**
     * 修改卡密信息
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    public int updateScriptCard(ScriptCard scriptCard);

    /**
     * 批量删除卡密信息
     * 
     * @param ids 需要删除的卡密信息主键集合
     * @return 结果
     */
    public int deleteScriptCardByIds(Long[] ids);

    /**
     * 删除卡密信息信息
     * 
     * @param id 卡密信息主键
     * @return 结果
     */
    public int deleteScriptCardById(Long id);

    /**
     * 校验卡密号是否唯一
     * 
     * @param scriptCard 卡密信息
     * @return 结果
     */
    public boolean checkCardNoUnique(ScriptCard scriptCard);

    /**
     * 通过卡密号查询卡密信息
     * 
     * @param cardNo 卡密号
     * @return 卡密信息
     */
    public ScriptCard selectScriptCardByCardNo(String cardNo);
}