package com.seeyon.kmp.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeImportTask;
import com.seeyon.kmp.service.KnowledgeImportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * kmp-parent
 * <p> File: KnowledgeImportTaskController.java </p>
 * <p> Description:  (导入)</p>
 * <p> Date: 2026/11/19 14:53</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@RestController
@RequestMapping("/kmp/knowledgeImportTask")
public class KnowledgeImportTaskController extends BaseController {

    @Autowired
    private KnowledgeImportTaskService knowledgeImportTaskService;

    /**
     * 获取知识导入任务列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return TableDataInfo
     */
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Long pageNum, @RequestParam Long pageSize) {
        return knowledgeImportTaskService.getList(pageNum, pageSize);
    }

    /**
     * 文件摘要重复检查
     * @param fileSha256 文件指纹SHA-256
     * @return 匹配结果(或未查到)
     */
    @GetMapping("/duplicate-check/{fileSha256}")
    public AjaxResult duplicateCheck(@PathVariable String fileSha256) {
        return knowledgeImportTaskService.checkByFileSha256(fileSha256);
    }

    /**
     * 后续处理开始前创建一条待上传任务
     * @param knowledgeImportTask 知识导入任务
     * @return AjaxResult
     */
    @PostMapping("/init")
    public AjaxResult init(@RequestBody KnowledgeImportTask knowledgeImportTask) {
        return knowledgeImportTaskService.init(knowledgeImportTask);
    }
}
