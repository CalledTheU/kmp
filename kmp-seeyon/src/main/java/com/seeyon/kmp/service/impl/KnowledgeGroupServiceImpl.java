package com.seeyon.kmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
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

    @Autowired
    private SysUserMapper sysUserMapper;

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

        return new TableDataInfo(knowledgeGroups, page.getTotal());
    }

    @Override
    public AjaxResult add(KnowledgeGroup knowledgeGroup) {
        try {
            // 添加知识组
            Long knowledgeGroupId = addOrUpdateKnowledgeGroup(knowledgeGroup);
            // 如果是默认知识组，则将其他知识组设为非默认
            if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault())) {
                updateOtherKnowledgeGroup(knowledgeGroup);
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

    private void updateOtherKnowledgeGroup(KnowledgeGroup knowledgeGroup) throws Exception {
        try {
            log.info("开始将除当前记录外的所有知识组设为非默认");
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
                log.info("知识组ID错误");
                throw new Exception("知识组ID错误");
            }

            // 更新知识组
            LambdaUpdateWrapper<KnowledgeGroup> wrapper = new LambdaUpdateWrapper<KnowledgeGroup>()
                    .eq(KnowledgeGroup::getId, knowledgeGroup.getId())
                    .set(KnowledgeGroup::getUpdateTime, LocalDateTime.now())
                    .set(KnowledgeGroup::getUpdateUserId, SecurityUtils.getLoginUser().getUserId())
                    .set(KnowledgeGroup::getIsDefault, knowledgeGroup.getIsDefault())
                    .set(KnowledgeGroup::getKnowledgeGroupDesc, knowledgeGroup.getKnowledgeGroupDesc())
                    .set(KnowledgeGroup::getKnowledgeGroupName, knowledgeGroup.getKnowledgeGroupName());

            boolean update = update(wrapper);
            log.info("更新知识组结果：{}", update);
            if (!update) {
                log.info("更新知识组失败");
                throw new Exception("更新知识组失败");
            }
            // 如果是默认知识组，则将其他知识组设为非默认
            if (Integer.valueOf(1).equals(knowledgeGroup.getIsDefault())) {
                updateOtherKnowledgeGroup(knowledgeGroup);
            }
        } catch (Exception e) {
            throw new Exception("更新知识组失败", e);
        }
    }

    @Override
    public AjaxResult delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.error("知识组ID错误: {}", id);
            return AjaxResult.error("知识组ID错误");
        }

        long groupId;
        try {
            groupId = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            log.error("知识组ID格式错误: {}", id, e);
            return AjaxResult.error("知识组ID错误");
        }

        if (groupId <= 0) {
            log.error("知识组ID错误: {}", id);
            return AjaxResult.error("知识组ID错误");
        }

        if (knowledgeGroupMapper.selectById(groupId).getIsDefault() == 1){
            throw new ServiceException("默认知识组不能删除");
        }

        LambdaUpdateWrapper<KnowledgeGroup> wrapper = new LambdaUpdateWrapper<KnowledgeGroup>()
                .set(KnowledgeGroup::getIsDeleted, 1)
                .eq(KnowledgeGroup::getId, groupId)
                .eq(KnowledgeGroup::getIsDeleted, 0);

        boolean update = update(wrapper);
        log.info("删除知识组 id={}, 结果={}", groupId, update);

        if (!update) {
            return AjaxResult.error("删除失败：记录不存在或已删除");
        }
        return AjaxResult.success("删除成功!");
    }

    @Override
    public AjaxResult get(String id) {
        KnowledgeGroup knowledgeGroup = knowledgeGroupMapper.selectById(id);
        // 获取更新用户名称
        if (knowledgeGroup.getUpdateUserId() != null && knowledgeGroup.getUpdateUserId() > 0) {
            String updateUserName = sysUserMapper.selectUserById(knowledgeGroup.getUpdateUserId()).getUserName();
            knowledgeGroup.setUpdateUserName(updateUserName);
        }
        // 获取创建用户名称
        if (knowledgeGroup.getCreateUserId() != null && knowledgeGroup.getCreateUserId() > 0) {
            String createUserName = sysUserMapper.selectUserById(knowledgeGroup.getCreateUserId()).getUserName();
            knowledgeGroup.setCreateUserName(createUserName);
        }
        return AjaxResult.success(knowledgeGroup);
    }

    @Override
    public List<KnowledgeGroup> getAllGroupOptions() {
                // 查询所有组
        LambdaQueryWrapper<KnowledgeGroup> wrapper = new LambdaQueryWrapper<KnowledgeGroup>();
        wrapper.select(KnowledgeGroup::getId, KnowledgeGroup::getKnowledgeGroupName, KnowledgeGroup::getIsDefault)
                .orderByDesc(KnowledgeGroup::getIsDefault)
                .orderByDesc(KnowledgeGroup::getCreateTime);
        return knowledgeGroupMapper.selectList(wrapper);
    }

}
