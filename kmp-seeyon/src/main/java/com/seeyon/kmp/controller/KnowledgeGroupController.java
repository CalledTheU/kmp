package com.seeyon.kmp.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeGroup;
import com.seeyon.kmp.service.KnowledgeGroupService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * kmp-parent
 * <p> File: KnowledgeGroupController.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/17 18:53</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@RestController
@RequestMapping("/kmp/knowledgeGroup")
public class KnowledgeGroupController extends BaseController {
    @Autowired
    private KnowledgeGroupService knowledgeGroupService;

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Long pageNum, @RequestParam Long pageSize, String knowledgeGroupName) {
        return knowledgeGroupService.list(pageNum, pageSize, knowledgeGroupName);
    }

    @PostMapping()
    public AjaxResult add(@RequestBody KnowledgeGroup knowledgeGroup) {
        return knowledgeGroupService.add(knowledgeGroup);
    }

    @PutMapping()
    public AjaxResult update(@RequestBody KnowledgeGroup knowledgeGroup) {
        return knowledgeGroupService.update(knowledgeGroup);
    }

    @Delete("{id}")
    public AjaxResult update(@PathVariable String id) {
        return knowledgeGroupService.delete(id);
    }

}
