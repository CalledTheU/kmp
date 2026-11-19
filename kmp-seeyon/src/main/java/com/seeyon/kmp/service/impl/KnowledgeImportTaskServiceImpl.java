package com.seeyon.kmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysUserMapper;
import com.seeyon.kmp.constant.KnowledgeUnitConstants;
import com.seeyon.kmp.entity.KnowledgeImportTask;
import com.seeyon.kmp.entity.KnowledgeUnit;
import com.seeyon.kmp.mapper.KnowledgeImportTaskMapper;
import com.seeyon.kmp.mapper.KnowledgeUnitMapper;
import com.seeyon.kmp.service.KnowledgeImportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
@Service
public class KnowledgeImportTaskServiceImpl extends ServiceImpl<KnowledgeImportTaskMapper, KnowledgeImportTask> implements KnowledgeImportTaskService {
    @Autowired
    private KnowledgeImportTaskMapper knowledgeImportTaskMapper;
    @Autowired
    private KnowledgeUnitMapper knowledgeUnitMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public TableDataInfo getList(Long pageNum, Long pageSize) {
        Page<KnowledgeImportTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeImportTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(KnowledgeImportTask::getCreateTime)
                .orderByDesc(KnowledgeImportTask::getId);

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
        if (normalizedSha256 == null || !KnowledgeUnitConstants.SHA256_PATTERN.matcher(normalizedSha256).matches())
        {
            throw new ServiceException("文件 SHA-256 格式不正确");
        }
        LambdaQueryWrapper<KnowledgeUnit> queryWrapper = new LambdaQueryWrapper<>();
        // 添加文件指纹SHA-256查询条件
        queryWrapper.eq(KnowledgeUnit::getFileSha256,normalizedSha256)
                .select(KnowledgeUnit::getKnowledgeUnitName,KnowledgeUnit::getId)
                .last("limit 1");
        KnowledgeUnit knowledgeUnit = knowledgeUnitMapper.selectOne(queryWrapper);
        Map<String,Object> result = new HashMap<>();
        // duplicate标识:是否重复
        result.put("duplicate", knowledgeUnit != null);
        // 根据SHA-256查到内容,表示重复,返回ID和Name
        if (knowledgeUnit != null){
            result.put("knowledgeUnitId", knowledgeUnit.getId());
            result.put("knowledgeUnitName", knowledgeUnit.getKnowledgeUnitName());
        }
        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult init(KnowledgeImportTask knowledgeImportTask) {
        return null;
    }
}
