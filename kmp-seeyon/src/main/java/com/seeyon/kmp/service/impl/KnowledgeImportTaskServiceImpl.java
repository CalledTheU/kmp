package com.seeyon.kmp.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.seeyon.kmp.config.KmpConfig;
import com.seeyon.kmp.constant.KnowledgeImportConstants;
import com.seeyon.kmp.constant.KnowledgeUnitConstants;
import com.seeyon.kmp.dto.ExternalKnowledgeTaskStatus;
import com.seeyon.kmp.entity.KnowledgeImportTask;
import com.seeyon.kmp.entity.KnowledgeUnit;
import com.seeyon.kmp.mapper.KnowledgeImportTaskMapper;
import com.seeyon.kmp.mapper.KnowledgeUnitMapper;
import com.seeyon.kmp.service.KnowledgeImportTaskService;
import com.seeyon.kmp.service.KnowledgeUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * kmp-parent
 * <p> File: KnowledgeImportTaskServiceImpl.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/19 14:56</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@Slf4j
@Service
public class KnowledgeImportTaskServiceImpl extends ServiceImpl<KnowledgeImportTaskMapper, KnowledgeImportTask> implements KnowledgeImportTaskService {
    @Autowired
    private KnowledgeImportTaskMapper knowledgeImportTaskMapper;
    @Autowired
    private KnowledgeUnitMapper knowledgeUnitMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Qualifier("knowledgeSystemRestClient")
    @Autowired
    private RestClient restClient;
    @Autowired
    private KmpConfig kmpConfig;

    @Autowired
    private KnowledgeUnitService knowledgeUnitService;

    @Override
    public TableDataInfo getList(Long pageNum, Long pageSize) {
        Page<KnowledgeImportTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeImportTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(KnowledgeImportTask::getCreateTime);

        knowledgeImportTaskMapper.selectPage(page, queryWrapper);

        for (KnowledgeImportTask task : page.getRecords()) {
            if (task.getCreateUserId() == null) {
                continue;
            }
            SysUser user = sysUserMapper.selectUserById(task.getCreateUserId());
            if (user != null) {
                task.setCreateUserName(user.getNickName());
            }
        }

        return new TableDataInfo(page.getRecords(), page.getTotal());
    }

    @Override
    public AjaxResult checkByFileSha256(String fileSha256) {
        // 将文件指纹SHA-256转换为小写并去除前后空格
        String normalizedSha256 = fileSha256 == null ? null : fileSha256.trim().toLowerCase();
        if (normalizedSha256 == null || !KnowledgeUnitConstants.SHA256_PATTERN.matcher(normalizedSha256).matches()) {
            throw new ServiceException("文件 SHA-256 格式不正确");
        }
        LambdaQueryWrapper<KnowledgeUnit> queryWrapper = new LambdaQueryWrapper<>();
        // 添加文件指纹SHA-256查询条件
        queryWrapper.eq(KnowledgeUnit::getFileSha256, normalizedSha256)
                .select(KnowledgeUnit::getKnowledgeUnitName, KnowledgeUnit::getId)
                .last("limit 1");
        KnowledgeUnit knowledgeUnit = knowledgeUnitMapper.selectOne(queryWrapper);
        Map<String, Object> result = new HashMap<>();
        // duplicate标识:是否重复
        result.put("duplicate", knowledgeUnit != null);
        // 根据SHA-256查到内容,表示重复,返回ID和Name
        if (knowledgeUnit != null) {
            result.put("knowledgeUnitId", knowledgeUnit.getId());
            result.put("knowledgeUnitName", knowledgeUnit.getKnowledgeUnitName());
        }
        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult init(KnowledgeImportTask knowledgeImportTask) {
        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getUserId();
        knowledgeImportTask.setCreateUserId(currentUserId);
        knowledgeImportTask.setCreateTime(LocalDateTime.now());
        knowledgeImportTask.setTaskStatus(KnowledgeImportConstants.TASK_STATUS_PENDING);
        knowledgeImportTaskMapper.insert(knowledgeImportTask);
        return AjaxResult.success(knowledgeImportTask.getId());
    }

    @Override
    public AjaxResult bindExternalTask(Long id, KnowledgeImportTask externalTaskId) {
        // 设置更新人的id
        externalTaskId.setUpdateUserId(SecurityUtils.getUserId());
        // 设置更新时间
        externalTaskId.setUpdateTime(LocalDateTime.now());
        // 根据ID查询任务
        LambdaUpdateWrapper<KnowledgeImportTask> wrapper = new LambdaUpdateWrapper<>();
        log.info("externalTaskId:{}", externalTaskId);
        wrapper.eq(KnowledgeImportTask::getId, id)
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PENDING)
                .isNull(KnowledgeImportTask::getExternalTaskId)
                .set(KnowledgeImportTask::getExternalTaskId, externalTaskId.getExternalTaskId())
                .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .set(KnowledgeImportTask::getUpdateTime, LocalDateTime.now())
                .set(KnowledgeImportTask::getUpdateUserId, SecurityUtils.getUserId());

        int update = baseMapper.update(externalTaskId, wrapper);
        if (update != 1) throw new ServiceException("绑定外部任务失败!");
        return AjaxResult.success(externalTaskId);
    }

    @Override
    public void pollProcessingTasks() {
        // 按照更新时间排序(正序)查询所有状态为处理中的任务
        LambdaQueryWrapper<KnowledgeImportTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(KnowledgeImportTask::getUpdateTime)
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .last("limit " + kmpConfig.getKnowledgeImportBatchSize());
        List<KnowledgeImportTask> tasks = knowledgeImportTaskMapper.selectList(queryWrapper);

        // 验证是否为空
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        // 遍历任务列表
        for (KnowledgeImportTask task : tasks) {
            updateKnowledgeBaseData(task);
        }
    }

    private void updateKnowledgeBaseData(KnowledgeImportTask task) {
        ExternalKnowledgeTaskStatus externalKnowledgeTaskInfo = null;
        try {
            // 远程调用查询任务状态的接口
            externalKnowledgeTaskInfo =
                    restClient.get().uri("/status/{task_id}", task.getExternalTaskId())
                            .retrieve().body(ExternalKnowledgeTaskStatus.class);
            // 获取任务状态
            String status = externalKnowledgeTaskInfo.getStatus();
            Integer taskStatus = switch (status) {
                case "completed" -> KnowledgeImportConstants.TASK_STATUS_SUCCESS;
                case "failed" -> KnowledgeImportConstants.TASK_STATUS_FAILED;
                default -> KnowledgeImportConstants.TASK_STATUS_PROCESSING;
            };
            // 根据任务状态更新任务
            updateKnowledgeBaseData(task, externalKnowledgeTaskInfo, taskStatus);
        } catch (Exception e) {
            // 处理异常状况
            log.error("处理知识导入任务异常，externalTaskId={}", task.getExternalTaskId(), e);
            this.markImportTaskFailed(task, externalKnowledgeTaskInfo, "处理知识导入任务异常");
        }
    }

    private void updateKnowledgeBaseData(KnowledgeImportTask task, ExternalKnowledgeTaskStatus externalKnowledgeTaskInfo, Integer taskStatus) {
        // 创建知识单元,获取知识单元ID
        Long knowledgeUnitId = knowledgeUnitService.createFromCompletedImport(task, externalKnowledgeTaskInfo);
        LambdaUpdateWrapper<KnowledgeImportTask> updateWrapper = new LambdaUpdateWrapper<>();

        updateWrapper.eq(KnowledgeImportTask::getId, task.getId())
                .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                .isNull(KnowledgeImportTask::getKnowledgeUnitId)
                .set(KnowledgeImportTask::getFailureReason, null)
                .set(KnowledgeImportTask::getUpdateUserId, SecurityUtils.getUserId())
                .set(KnowledgeImportTask::getUpdateTime, LocalDateTime.now());
        if (KnowledgeImportConstants.TASK_STATUS_SUCCESS.equals(taskStatus)) {
            // 导入成功
            updateWrapper.set(KnowledgeImportTask::getTaskInfo, JSON.toJSONString(externalKnowledgeTaskInfo))
                    .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_SUCCESS)
                    .set(KnowledgeImportTask::getKnowledgeUnitId, knowledgeUnitId)
                    .set(KnowledgeImportTask::getFinishTime, LocalDateTime.now())
                    .set(KnowledgeImportTask::getDurationMs, Duration.between(task.getCreateTime(), LocalDateTime.now()).toMillis());
        } else if (KnowledgeImportConstants.TASK_STATUS_PROCESSING.equals(taskStatus)) {
            // 导入进行中
            updateWrapper.set(KnowledgeImportTask::getTaskStatus, taskStatus)
                    .set(KnowledgeImportTask::getTaskInfo, JSON.toJSONString(externalKnowledgeTaskInfo));
        } else {
            // 导入失败
            updateWrapper.set(KnowledgeImportTask::getTaskStatus, taskStatus)
                    .set(KnowledgeImportTask::getTaskInfo, JSON.toJSONString(externalKnowledgeTaskInfo))
                    .set(KnowledgeImportTask::getDurationMs, Duration.between(task.getCreateTime(), LocalDateTime.now()).toMillis())
                    .set(KnowledgeImportTask::getFinishTime, LocalDateTime.now());
        }
        baseMapper.update(updateWrapper);
    }

    /**
     * 出现异常时修改任务状态为失败
     * @param task
     * @param externalStatus
     * @param failureReason
     */
    private void markImportTaskFailed(KnowledgeImportTask task, ExternalKnowledgeTaskStatus externalStatus, String failureReason)
    {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<KnowledgeImportTask> updateWrapper =
                new LambdaUpdateWrapper<KnowledgeImportTask>()
                        .eq(KnowledgeImportTask::getId, task.getId())
                        .eq(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_PROCESSING)
                        .isNull(KnowledgeImportTask::getKnowledgeUnitId)
                        .set(KnowledgeImportTask::getTaskStatus, KnowledgeImportConstants.TASK_STATUS_FAILED)
                        .set(KnowledgeImportTask::getTaskInfo, JSON.toJSONString(externalStatus))
                        .set(KnowledgeImportTask::getFailureReason, failureReason)
                        .set(KnowledgeImportTask::getFinishTime, now)
                        .set(KnowledgeImportTask::getDurationMs, Duration.between(task.getCreateTime(), now).toMillis())
                        .set(KnowledgeImportTask::getUpdateTime, now);
        this.update(updateWrapper);
    }


}
