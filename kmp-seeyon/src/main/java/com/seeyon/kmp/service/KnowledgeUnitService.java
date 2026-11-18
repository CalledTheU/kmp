package com.seeyon.kmp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.seeyon.kmp.entity.KnowledgeUnit;

/**
 * kmp-parent
 * <p> File: KnowledgeUnitService.java </p>
 * <p> Description:  (知识单元服务接口)</p>
 * <p> Date: 2026/11/18 10:42</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
public interface KnowledgeUnitService extends IService<KnowledgeUnit> {

    /**
     * 获取知识单元列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param knowledgeUnitName 知识单元名称
     * @param knowledgeGroupId 知识组ID
     * @return 知识单元列表
     */
    TableDataInfo getList(int pageNum, int pageSize, String knowledgeUnitName, String knowledgeGroupId);

    /**
     * 根据ID获取知识单元
     * @param id ID
      * @return 知识单元
     */
    AjaxResult getKnowledgeUnitById(int id);

    /**
     * 更新知识单元
      * @param knowledgeUnit 知识单元
      * @return 更新结果
     */
    AjaxResult updateKnowledgeUnit(KnowledgeUnit knowledgeUnit);

    /**
     * 删除知识单元
     * @param id ID
     * @return 删除结果
     */
    AjaxResult deleteKnowledgeUnit(int id);
}
