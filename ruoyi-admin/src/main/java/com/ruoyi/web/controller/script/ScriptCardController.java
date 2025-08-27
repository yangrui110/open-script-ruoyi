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
import com.ruoyi.script.domain.ScriptCard;
import com.ruoyi.script.service.IScriptCardService;

/**
 * 卡密信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/script/card")
public class ScriptCardController extends BaseController
{
    @Autowired
    private IScriptCardService scriptCardService;

    /**
     * 获取卡密列表
     */
    @PreAuthorize("@ss.hasPermi('script:card:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScriptCard scriptCard)
    {
        startPage();
        List<ScriptCard> list = scriptCardService.selectScriptCardList(scriptCard);
        return getDataTable(list);
    }
    
    /**
     * 导出卡密列表
     */
    @Log(title = "卡密管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('script:card:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ScriptCard scriptCard)
    {
        List<ScriptCard> list = scriptCardService.selectScriptCardList(scriptCard);
        ExcelUtil<ScriptCard> util = new ExcelUtil<ScriptCard>(ScriptCard.class);
        util.exportExcel(response, list, "卡密数据");
    }

    /**
     * 根据卡密编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('script:card:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(scriptCardService.selectScriptCardById(id));
    }

    /**
     * 新增卡密
     */
    @PreAuthorize("@ss.hasPermi('script:card:add')")
    @Log(title = "卡密管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ScriptCard scriptCard)
    {
        if (!scriptCardService.checkCardNoUnique(scriptCard))
        {
            return error("新增卡密'" + scriptCard.getCardNo() + "'失败，卡密号已存在");
        }
        scriptCard.setCreateBy(getUsername());
        return toAjax(scriptCardService.insertScriptCard(scriptCard));
    }

    /**
     * 修改卡密
     */
    @PreAuthorize("@ss.hasPermi('script:card:edit')")
    @Log(title = "卡密管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ScriptCard scriptCard)
    {
        if (!scriptCardService.checkCardNoUnique(scriptCard))
        {
            return error("修改卡密'" + scriptCard.getCardNo() + "'失败，卡密号已存在");
        }
        scriptCard.setUpdateBy(getUsername());
        return toAjax(scriptCardService.updateScriptCard(scriptCard));
    }

    /**
     * 删除卡密
     */
    @PreAuthorize("@ss.hasPermi('script:card:remove')")
    @Log(title = "卡密管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(scriptCardService.deleteScriptCardByIds(ids));
    }

    /**
     * 获取卡密选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        List<ScriptCard> scriptCards = scriptCardService.selectScriptCardAll();
        return success(scriptCards);
    }

    /**
     * 根据卡密号查询卡密信息
     */
    @GetMapping("/getByCardNo/{cardNo}")
    public AjaxResult getByCardNo(@PathVariable String cardNo)
    {
        return success(scriptCardService.selectScriptCardByCardNo(cardNo));
    }
}