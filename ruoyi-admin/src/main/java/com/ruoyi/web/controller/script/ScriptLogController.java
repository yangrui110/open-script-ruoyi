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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.script.domain.ScriptLog;
import com.ruoyi.script.service.IScriptLogService;

/**
 * 脚本日志信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/script/log")
public class ScriptLogController extends BaseController
{
    @Autowired
    private IScriptLogService scriptLogService;

    /**
     * 获取脚本日志列表
     */
    @PreAuthorize("@ss.hasPermi('script:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScriptLog scriptLog)
    {
        startPage();
        List<ScriptLog> list = scriptLogService.selectScriptLogList(scriptLog);
        return getDataTable(list);
    }
    
    /**
     * 导出脚本日志列表
     */
    @Log(title = "脚本日志管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('script:log:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ScriptLog scriptLog)
    {
        List<ScriptLog> list = scriptLogService.selectScriptLogList(scriptLog);
        ExcelUtil<ScriptLog> util = new ExcelUtil<ScriptLog>(ScriptLog.class);
        util.exportExcel(response, list, "脚本日志数据");
    }

    /**
     * 根据脚本日志编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('script:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(scriptLogService.selectScriptLogById(id));
    }

    /**
     * 根据设备ID获取脚本日志列表
     */
    @GetMapping("/android/{androidId}")
    public AjaxResult getLogsByAndroidId(@PathVariable String androidId)
    {
        List<ScriptLog> list = scriptLogService.selectScriptLogByAndroidId(androidId);
        return success(list);
    }

    /**
     * 根据日志级别获取脚本日志列表
     */
    @GetMapping("/level/{level}")
    public AjaxResult getLogsByLevel(@PathVariable String level)
    {
        List<ScriptLog> list = scriptLogService.selectScriptLogByLevel(level);
        return success(list);
    }

    /**
     * 新增脚本日志
     */
    @PreAuthorize("@ss.hasPermi('script:log:add')")
    @Log(title = "脚本日志管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ScriptLog scriptLog)
    {
        scriptLog.setCreateBy(getUsername());
        return toAjax(scriptLogService.insertScriptLog(scriptLog));
    }

    /**
     * 修改脚本日志
     */
    @PreAuthorize("@ss.hasPermi('script:log:edit')")
    @Log(title = "脚本日志管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ScriptLog scriptLog)
    {
        scriptLog.setUpdateBy(getUsername());
        return toAjax(scriptLogService.updateScriptLog(scriptLog));
    }

    /**
     * 删除脚本日志
     */
    @PreAuthorize("@ss.hasPermi('script:log:remove')")
    @Log(title = "脚本日志管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(scriptLogService.deleteScriptLogByIds(ids));
    }

    /**
     * 清空指定设备的日志
     */
    @PreAuthorize("@ss.hasPermi('script:log:remove')")
    @Log(title = "脚本日志管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/android/{androidId}")
    public AjaxResult clearLogsByAndroidId(@PathVariable String androidId)
    {
        return toAjax(scriptLogService.deleteScriptLogByAndroidId(androidId));
    }

    /**
     * 清空指定日期之前的日志
     */
    @PreAuthorize("@ss.hasPermi('script:log:remove')")
    @Log(title = "脚本日志管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/before")
    public AjaxResult clearLogsBeforeDate(@RequestParam String beforeDate)
    {
        return toAjax(scriptLogService.deleteScriptLogBeforeDate(beforeDate));
    }

    /**
     * 批量上报脚本日志（供客户端调用）
     */
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<ScriptLog> scriptLogs)
    {
        int successCount = 0;
        for (ScriptLog scriptLog : scriptLogs)
        {
            try
            {
                scriptLog.setCreateBy("system");
                scriptLogService.insertScriptLog(scriptLog);
                successCount++;
            }
            catch (Exception e)
            {
                logger.error("批量上报日志失败: {}", e.getMessage());
            }
        }
        
        AjaxResult result = AjaxResult.success();
        result.put("successCount", successCount);
        result.put("totalCount", scriptLogs.size());
        return result;
    }

    /**
     * 获取脚本日志选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        List<ScriptLog> scriptLogs = scriptLogService.selectScriptLogAll();
        return success(scriptLogs);
    }
}