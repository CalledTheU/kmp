package com.seeyon.kmp.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeUnit;
import com.seeyon.kmp.service.KnowledgeUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * kmp-parent
 * <p> File: KnowledgeUnitController.java </p>
 * <p> Description:  (知识单元控制器)</p>
 * <p> Date: 2026/11/18 10:39</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@RestController
@RequestMapping("/kmp/knowledgeUnit")
public class KnowledgeUnitController extends BaseController {

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam int pageNum, @RequestParam int pageSize,
                              String knowledgeUnitName, String knowledgeGroupId) {
        return knowledgeUnitService.getList(pageNum, pageSize, knowledgeUnitName, knowledgeGroupId);
    }

    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable int id) {
        return knowledgeUnitService.getKnowledgeUnitById(id);
    }

    @PutMapping
    public AjaxResult update(@Validated(KnowledgeUnit.Edit.class) @RequestBody KnowledgeUnit knowledgeUnit) {
        return knowledgeUnitService.updateKnowledgeUnit(knowledgeUnit);
    }

    @DeleteMapping("{id}")
    public AjaxResult delete(@PathVariable int id) {
        return knowledgeUnitService.deleteKnowledgeUnit(id);
    }
}
