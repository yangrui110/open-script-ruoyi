package com.ruoyi.web.controller.script;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.script.domain.ScriptGame;
import com.ruoyi.script.service.IScriptGameService;

/**
 * 游戏信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/script/game")
public class ScriptGameController extends BaseController
{
    @Autowired
    private IScriptGameService scriptGameService;

    /**
     * 获取游戏列表
     */
    @PreAuthorize("@ss.hasPermi('script:game:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScriptGame scriptGame)
    {
        startPage();
        List<ScriptGame> list = scriptGameService.selectScriptGameList(scriptGame);
        return getDataTable(list);
    }
    
    /**
     * 导出游戏列表
     */
    @Log(title = "游戏管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('script:game:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ScriptGame scriptGame)
    {
        List<ScriptGame> list = scriptGameService.selectScriptGameList(scriptGame);
        ExcelUtil<ScriptGame> util = new ExcelUtil<ScriptGame>(ScriptGame.class);
        util.exportExcel(response, list, "游戏数据");
    }

    /**
     * 根据游戏编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('script:game:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(scriptGameService.selectScriptGameById(id));
    }

    /**
     * 新增游戏
     */
    @PreAuthorize("@ss.hasPermi('script:game:add')")
    @Log(title = "游戏管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ScriptGame scriptGame)
    {
        if (!scriptGameService.checkTitleUnique(scriptGame))
        {
            return error("新增游戏'" + scriptGame.getTitle() + "'失败，游戏名称已存在");
        }
        scriptGame.setCreateBy(getUsername());
        return toAjax(scriptGameService.insertScriptGame(scriptGame));
    }

    /**
     * 修改游戏
     */
    @PreAuthorize("@ss.hasPermi('script:game:edit')")
    @Log(title = "游戏管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ScriptGame scriptGame)
    {
        if (!scriptGameService.checkTitleUnique(scriptGame))
        {
            return error("修改游戏'" + scriptGame.getTitle() + "'失败，游戏名称已存在");
        }
        scriptGame.setUpdateBy(getUsername());
        return toAjax(scriptGameService.updateScriptGame(scriptGame));
    }

    /**
     * 删除游戏
     */
    @PreAuthorize("@ss.hasPermi('script:game:remove')")
    @Log(title = "游戏管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(scriptGameService.deleteScriptGameByIds(ids));
    }

    /**
     * 获取游戏选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        List<ScriptGame> scriptGames = scriptGameService.selectScriptGameAll();
        return success(scriptGames);
    }
}