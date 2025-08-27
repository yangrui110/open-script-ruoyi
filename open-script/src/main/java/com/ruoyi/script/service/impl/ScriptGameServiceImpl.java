package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.script.domain.ScriptGame;
import com.ruoyi.script.mapper.ScriptGameMapper;
import com.ruoyi.script.service.IScriptGameService;

/**
 * 游戏信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptGameServiceImpl implements IScriptGameService
{
    @Autowired
    private ScriptGameMapper scriptGameMapper;

    /**
     * 查询游戏信息
     * 
     * @param id 游戏信息主键
     * @return 游戏信息
     */
    @Override
    public ScriptGame selectScriptGameById(Long id)
    {
        return scriptGameMapper.selectScriptGameById(id);
    }

    /**
     * 查询游戏信息列表
     * 
     * @param scriptGame 游戏信息
     * @return 游戏信息
     */
    @Override
    public List<ScriptGame> selectScriptGameList(ScriptGame scriptGame)
    {
        return scriptGameMapper.selectScriptGameList(scriptGame);
    }

    /**
     * 查询所有游戏信息
     * 
     * @return 游戏信息集合
     */
    @Override
    public List<ScriptGame> selectScriptGameAll()
    {
        return scriptGameMapper.selectScriptGameAll();
    }

    /**
     * 新增游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    @Override
    public int insertScriptGame(ScriptGame scriptGame)
    {
        return scriptGameMapper.insertScriptGame(scriptGame);
    }

    /**
     * 修改游戏信息
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    @Override
    public int updateScriptGame(ScriptGame scriptGame)
    {
        return scriptGameMapper.updateScriptGame(scriptGame);
    }

    /**
     * 批量删除游戏信息
     * 
     * @param ids 需要删除的游戏信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptGameByIds(Long[] ids)
    {
        return scriptGameMapper.deleteScriptGameByIds(ids);
    }

    /**
     * 删除游戏信息信息
     * 
     * @param id 游戏信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptGameById(Long id)
    {
        return scriptGameMapper.deleteScriptGameById(id);
    }

    /**
     * 校验游戏名称是否唯一
     * 
     * @param scriptGame 游戏信息
     * @return 结果
     */
    @Override
    public boolean checkTitleUnique(ScriptGame scriptGame)
    {
        Long id = StringUtils.isNull(scriptGame.getId()) ? -1L : scriptGame.getId();
        ScriptGame info = scriptGameMapper.checkTitleUnique(scriptGame.getTitle());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}