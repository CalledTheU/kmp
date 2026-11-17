package com.seeyon.kmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.seeyon.kmp.entity.KnowledgeGroup;
import com.seeyon.kmp.mapper.KnowledgeGroupMapper;
import com.seeyon.kmp.service.KnowledgeGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * kmp-parent
 * <p> File: KnowledgeGroupServiceImpl.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/17 18:51</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@Slf4j
@Service
public class KnowledgeGroupServiceImpl extends ServiceImpl<KnowledgeGroupMapper, KnowledgeGroup> implements KnowledgeGroupService {


    @Autowired
    private KnowledgeGroupMapper knowledgeGroupMapper;

    @Override
    public TableDataInfo list(Long pageNum, Long pageSize, String knowledgeGroupName) {

        Page<KnowledgeGroup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeGroup> wrapper = new LambdaQueryWrapper<KnowledgeGroup>();
        // 按照创建时间降序排列
        wrapper.orderByDesc(KnowledgeGroup::getCreateTime);
        // 如果知识组名称不为空，则按照知识组名称模糊查询
        if (knowledgeGroupName != null && !knowledgeGroupName.isEmpty()) {
            wrapper.like(KnowledgeGroup::getKnowledgeGroupName, knowledgeGroupName);
        }

        List<KnowledgeGroup> knowledgeGroups = knowledgeGroupMapper.selectList(page, wrapper);

        return new TableDataInfo(knowledgeGroups, knowledgeGroups.size());
    }

    @Override
    public AjaxResult add(KnowledgeGroup knowledgeGroup) {
        try {
            // 添加知识组
            Long knowledgeGroupId = addOrUpdateKnowledgeGroup(knowledgeGroup);
            // 如果是默认知识组，则将其他知识组设为非默认
            if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault())) {
                updateOtherKnowledgeGroup();
            }
            return AjaxResult.success(knowledgeGroupId);
        } catch (Exception e) {
            return AjaxResult.error(500, e.getMessage());
        }
    }

    @Override
    public AjaxResult update(KnowledgeGroup knowledgeGroup) {
        try {
            addOrUpdateKnowledgeGroup(knowledgeGroup);
            return AjaxResult.success("更新成功");
        } catch (Exception e) {
            return AjaxResult.error(500, e.getMessage());
        }
    }

    private void updateOtherKnowledgeGroup() throws Exception {
        try {
            log.info("开始将除当前记录外的所有知识组设为非默认");
            KnowledgeGroup knowledgeGroup = new KnowledgeGroup();
            // 把除当前记录外的所有知识组设为非默认
            LambdaUpdateWrapper<KnowledgeGroup> wrapper = new LambdaUpdateWrapper<KnowledgeGroup>()
                    .set(KnowledgeGroup::getUpdateTime, LocalDateTime.now()) // 设置更新时间
                    .set(KnowledgeGroup::getIsDefault, "0")            // 显式指定要更新的字段
                    .ne(KnowledgeGroup::getId, knowledgeGroup.getId()); // 排除当前这条
            boolean update = update(wrapper);// IService 的 update(Wrapper) 方法，等价于 update(null, wrapper)
            log.info("将除当前记录外的所有知识组设为非默认结果：{}", update);
        } catch (Exception e) {
            throw new Exception("将除当前记录外的所有知识组设为非默认失败", e);
        }
    }

    private Long addOrUpdateKnowledgeGroup(KnowledgeGroup knowledgeGroup) throws Exception {
        try {
            if (knowledgeGroup.getId() != null && knowledgeGroup.getId() > 0) {
                // 更新知识组
                updateByGroupId(knowledgeGroup);
                return knowledgeGroup.getId();
            }
            knowledgeGroup.setUpdateTime(null);
            knowledgeGroup.setIsDeleted(0);
            // 设置空id,防止小人
            knowledgeGroup.setId(null);
            // 设置创建时间
            knowledgeGroup.setCreateTime(LocalDateTime.now());
            // 设置空更新人
            knowledgeGroup.setUpdateUserId(null);
            // 设置创建用户ID
            knowledgeGroup.setCreateUserId(SecurityUtils.getLoginUser().getUserId());
            // 保存知识组
            boolean save = save(knowledgeGroup);
            log.info("保存知识组结果：{}", save);
            return knowledgeGroup.getId();
        } catch (Exception e) {
            throw new Exception("保存知识组失败", e);
        }
    }

    private void updateByGroupId(KnowledgeGroup knowledgeGroup) throws Exception {
        try {
            log.info("开始更新知识组");
            if (knowledgeGroup.getId() == null || knowledgeGroup.getId() <= 0) {
                log.error("知识组ID错误");
                throw new Exception("知识组ID错误");
            }
            // 更新知识组
            LambdaQueryWrapper<KnowledgeGroup> wrapper = new LambdaQueryWrapper<KnowledgeGroup>()
                    .eq(KnowledgeGroup::getId, knowledgeGroup.getId());

            boolean update = update(knowledgeGroup, wrapper);
            log.info("更新知识组结果：{}", update);
        } catch (Exception e) {
            throw new Exception("更新知识组失败", e);
        }
    }

}
