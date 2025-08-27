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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.script.domain.ScriptVersionControl;
import com.ruoyi.script.service.IScriptVersionControlService;

/**
 * 脚本版本信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/script/version")
public class ScriptVersionControlController extends BaseController
{
    @Autowired
    private IScriptVersionControlService scriptVersionControlService;

    /**
     * 获取脚本版本列表
     */
    @PreAuthorize("@ss.hasPermi('script:version:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScriptVersionControl scriptVersionControl)
    {
        startPage();
        List<ScriptVersionControl> list = scriptVersionControlService.selectScriptVersionControlList(scriptVersionControl);
        return getDataTable(list);
    }
    
    /**
     * 导出脚本版本列表
     */
    @Log(title = "脚本版本管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('script:version:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ScriptVersionControl scriptVersionControl)
    {
        List<ScriptVersionControl> list = scriptVersionControlService.selectScriptVersionControlList(scriptVersionControl);
        ExcelUtil<ScriptVersionControl> util = new ExcelUtil<ScriptVersionControl>(ScriptVersionControl.class);
        util.exportExcel(response, list, "脚本版本数据");
    }

    /**
     * 根据脚本版本编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('script:version:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(scriptVersionControlService.selectScriptVersionControlById(id));
    }

    /**
     * 根据游戏ID获取脚本版本列表
     */
    @GetMapping("/game/{gameId}")
    public AjaxResult getVersionsByGameId(@PathVariable Long gameId)
    {
        List<ScriptVersionControl> list = scriptVersionControlService.selectScriptVersionControlByGameId(gameId);
        return success(list);
    }

    /**
     * 获取指定游戏的最新版本号
     */
    @GetMapping("/maxVersion/{gameId}")
    public AjaxResult getMaxVersionByGameId(@PathVariable Long gameId)
    {
        Integer maxVersion = scriptVersionControlService.selectMaxVersionByGameId(gameId);
        return success(maxVersion);
    }

    /**
     * 新增脚本版本
     */
    @PreAuthorize("@ss.hasPermi('script:version:add')")
    @Log(title = "脚本版本管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ScriptVersionControl scriptVersionControl)
    {
        scriptVersionControl.setCreateBy(getUsername());
        return toAjax(scriptVersionControlService.insertScriptVersionControl(scriptVersionControl));
    }

    /**
     * 修改脚本版本
     */
    @PreAuthorize("@ss.hasPermi('script:version:edit')")
    @Log(title = "脚本版本管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ScriptVersionControl scriptVersionControl)
    {
        scriptVersionControl.setUpdateBy(getUsername());
        return toAjax(scriptVersionControlService.updateScriptVersionControl(scriptVersionControl));
    }

    /**
     * 删除脚本版本
     */
    @PreAuthorize("@ss.hasPermi('script:version:remove')")
    @Log(title = "脚本版本管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(scriptVersionControlService.deleteScriptVersionControlByIds(ids));
    }

    /**
     * 获取脚本版本选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        List<ScriptVersionControl> scriptVersionControls = scriptVersionControlService.selectScriptVersionControlAll();
        return success(scriptVersionControls);
    }

    /**
     * 脚本文件上传
     */
    @PostMapping("/upload")
    public AjaxResult uploadScript(@RequestParam("file") MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath() + "/script";
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", fileName);
            ajax.put("fileName", fileName);
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }
}