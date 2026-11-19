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

    AjaxResult init(KnowledgeImportTask knowledgeImportTask);
}
