package com.seeyon.kmp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeGroup;

/**
 * kmp-parent
 * <p> File: KnowledgeGroupService.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/17 18:51</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
public interface KnowledgeGroupService extends IService<KnowledgeGroup> {

    /**
     * 查询知识分类列表
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param knowledgeGroupName 知识分类名称
     * @return 知识分类列表
     */
    TableDataInfo list( Long pageNum, Long pageSize, String knowledgeGroupName);

    /**
     * 添加知识分类
     *
     * @param knowledgeGroup 知识分类对象
     * @return 操作结果
     */
    AjaxResult add(KnowledgeGroup knowledgeGroup);

    /**
     * 修改知识分类
     *
     * @param knowledgeGroup 知识分类对象
     * @return 操作结果
     */
    AjaxResult update(KnowledgeGroup knowledgeGroup);
}
