package com.ruoyi.script.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.script.domain.ScriptVersionControl;
import com.ruoyi.script.domain.vo.ScriptVersionControlVo;
import com.ruoyi.script.mapper.ScriptVersionControlMapper;
import com.ruoyi.script.service.IScriptVersionControlService;

/**
 * 脚本版本信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ScriptVersionControlServiceImpl implements IScriptVersionControlService
{
    @Autowired
    private ScriptVersionControlMapper scriptVersionControlMapper;

    /**
     * 查询脚本版本信息
     * 
     * @param id 脚本版本信息主键
     * @return 脚本版本信息
     */
    @Override
    public ScriptVersionControl selectScriptVersionControlById(Long id)
    {
        return scriptVersionControlMapper.selectScriptVersionControlById(id);
    }

    /**
     * 查询脚本版本信息列表
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 脚本版本信息
     */
    @Override
    public List<ScriptVersionControl> selectScriptVersionControlList(ScriptVersionControl scriptVersionControl)
    {
        return scriptVersionControlMapper.selectScriptVersionControlList(scriptVersionControl);
    }

    /**
     * 查询所有脚本版本信息
     * 
     * @return 脚本版本信息集合
     */
    @Override
    public List<ScriptVersionControl> selectScriptVersionControlAll()
    {
        return scriptVersionControlMapper.selectScriptVersionControlAll();
    }

    /**
     * 通过游戏ID查询脚本版本列表
     * 
     * @param gameId 游戏ID
     * @return 脚本版本列表
     */
    @Override
    public List<ScriptVersionControl> selectScriptVersionControlByGameId(Long gameId)
    {
        return scriptVersionControlMapper.selectScriptVersionControlByGameId(gameId);
    }

    /**
     * 获取指定游戏的最新版本号
     * 
     * @param gameId 游戏ID
     * @return 最新版本号
     */
    @Override
    public Integer selectMaxVersionByGameId(Long gameId)
    {
        Integer maxVersion = scriptVersionControlMapper.selectMaxVersionByGameId(gameId);
        return maxVersion == null ? 0 : maxVersion;
    }

    /**
     * 新增脚本版本信息（自动生成版本号）
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    @Override
    public int insertScriptVersionControl(ScriptVersionControl scriptVersionControl)
    {
        // 自动生成版本号（在当前游戏的最大版本号基础上+1）
        Integer maxVersion = selectMaxVersionByGameId(scriptVersionControl.getGameId());
        scriptVersionControl.setVersion(maxVersion + 1);
        
        return scriptVersionControlMapper.insertScriptVersionControl(scriptVersionControl);
    }

    /**
     * 修改脚本版本信息
     * 
     * @param scriptVersionControl 脚本版本信息
     * @return 结果
     */
    @Override
    public int updateScriptVersionControl(ScriptVersionControl scriptVersionControl)
    {
        return scriptVersionControlMapper.updateScriptVersionControl(scriptVersionControl);
    }

    /**
     * 批量删除脚本版本信息
     * 
     * @param ids 需要删除的脚本版本信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptVersionControlByIds(Long[] ids)
    {
        return scriptVersionControlMapper.deleteScriptVersionControlByIds(ids);
    }

    /**
     * 删除脚本版本信息信息
     * 
     * @param id 脚本版本信息主键
     * @return 结果
     */
    @Override
    public int deleteScriptVersionControlById(Long id)
    {
        return scriptVersionControlMapper.deleteScriptVersionControlById(id);
    }

    /**
     * 获取指定游戏的最新版本信息
     * 
     * @param gameId 游戏ID
     * @return 最新版本信息
     */
    @Override
    public ScriptVersionControlVo getLatestVersion(Long gameId)
    {
        // 获取最新版本号
        Integer maxVersion = selectMaxVersionByGameId(gameId);
        if (maxVersion == null || maxVersion == 0)
        {
            return null;
        }

        // 根据游戏ID和版本号查询最新版本记录
        ScriptVersionControl latest = scriptVersionControlMapper.selectLatestVersionByGameId(gameId, maxVersion);
        if (latest == null)
        {
            return null;
        }

        // 转换为VO对象
        ScriptVersionControlVo vo = new ScriptVersionControlVo();
        vo.setId(latest.getId());
        vo.setTenantId(null); // ScriptVersionControl没有tenantId字段，设为null
        vo.setGameId(latest.getGameId());
        vo.setGameTitle(latest.getGameTitle());
        vo.setFileUrl(latest.getFileUrl());
        vo.setType(latest.getType());
        vo.setVersion(latest.getVersion());
        // 安全地转换createBy和updateBy
        try {
            if (latest.getCreateBy() != null && !latest.getCreateBy().isEmpty()) {
                vo.setCreateBy(Long.valueOf(latest.getCreateBy()));
            }
        } catch (NumberFormatException e) {
            vo.setCreateBy(null);
        }
        
        try {
            if (latest.getUpdateBy() != null && !latest.getUpdateBy().isEmpty()) {
                vo.setUpdateBy(Long.valueOf(latest.getUpdateBy()));
            }
        } catch (NumberFormatException e) {
            vo.setUpdateBy(null);
        }
        vo.setRemark(latest.getRemark());

        // 转换时间类型
        if (latest.getCreateTime() != null)
        {
            vo.setCreateTime(latest.getCreateTime().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        if (latest.getUpdateTime() != null)
        {
            vo.setUpdateTime(latest.getUpdateTime().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        // 设置文件类型描述
        if (latest.getType() != null)
        {
            vo.setTypeDesc(latest.getType() == 0 ? "单js文件" : "zip文件");
        }

        return vo;
    }
}