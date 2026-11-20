package com.seeyon.kmp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeImportTask;

/**
 * kmp-parent
 * <p> File: KnowledgeImportTaskService.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/19 14:56</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
public interface KnowledgeImportTaskService extends IService<KnowledgeImportTask> {
    /**
     * 获取知识导入任务列表并补充创建人信息
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return TableDataInfo
     */
    TableDataInfo getList(Long pageNum, Long pageSize);

    /**
     * 查询相同file_sha256的记录
      * @param fileSha256 文件指纹SHA-256
      * @return AjaxResult
     */
    AjaxResult checkByFileSha256(String fileSha256);

    /**
     * 初始化知识导入任务
     * @param knowledgeImportTask 知识导入任务
     * @return AjaxResult
     */
    AjaxResult init(KnowledgeImportTask knowledgeImportTask);

    /**
     * 绑定外部任务
     * @param id 知识导入任务ID
     * @param externalTaskId 外部任务ID
     * @return AjaxResult
     */
    AjaxResult bindExternalTask(Long id, KnowledgeImportTask externalTaskId);

    /**
     * 定时轮询处理中的任务状态
     */
    void pollProcessingTasks();
}
