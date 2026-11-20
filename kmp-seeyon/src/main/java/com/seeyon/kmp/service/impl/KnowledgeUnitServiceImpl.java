package com.seeyon.kmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.seeyon.kmp.dto.ExternalKnowledgeTaskStatus;
import com.seeyon.kmp.entity.KnowledgeGroup;
import com.seeyon.kmp.entity.KnowledgeImportTask;
import com.seeyon.kmp.entity.KnowledgeUnit;
import com.seeyon.kmp.mapper.KnowledgeGroupMapper;
import com.seeyon.kmp.mapper.KnowledgeUnitMapper;
import com.seeyon.kmp.service.KnowledgeUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * kmp-parent
 * <p> File: KnowledgeUnitServiceImpl.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/18 10:43</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@Service
public class KnowledgeUnitServiceImpl  extends ServiceImpl<KnowledgeUnitMapper, KnowledgeUnit> implements KnowledgeUnitService{

    @Autowired
    private KnowledgeUnitMapper knowledgeUnitMapper;

    @Autowired
    private KnowledgeGroupMapper knowledgeGroupMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${kmp.minio-public-endpoint}")
    private String minioEndpoint;

    @Override
    public TableDataInfo getList(int pageNum, int pageSize, String knowledgeUnitName, String knowledgeGroupId) {
        Page<KnowledgeUnit> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<KnowledgeUnit> wrapper = new LambdaQueryWrapper<>();

        if (knowledgeUnitName != null && !knowledgeUnitName.isEmpty()){
            wrapper.like(KnowledgeUnit::getKnowledgeUnitName, knowledgeUnitName);
        }

        if (knowledgeGroupId != null && !knowledgeGroupId.isEmpty()){
            wrapper.eq(KnowledgeUnit::getKnowledgeGroupId, knowledgeGroupId);
        }

        knowledgeUnitMapper.selectPage(page, wrapper);
        List<KnowledgeUnit> knowledgeUnits = page.getRecords();

        if (CollectionUtils.isEmpty(knowledgeUnits)){
            return new TableDataInfo();
        }

        // 获取所有知识组的id
        List<Long> collect = knowledgeUnits.stream().map(KnowledgeUnit::getKnowledgeGroupId).toList();
        // 根据id获取知识组列表
        List<KnowledgeGroup> knowledgeGroups = knowledgeGroupMapper.selectByIds(collect);
        // 变成知识组id和知识组的映射关系
        Map<Long, KnowledgeGroup> groupMap = knowledgeGroups.stream()
                .collect(Collectors.toMap(
                        KnowledgeGroup::getId,
                        Function.identity()
                ));


        for (KnowledgeUnit knowledgeUnit : knowledgeUnits) {
            Long groupId = knowledgeUnit.getKnowledgeGroupId();
            //KnowledgeGroup knowledgeGroup = knowledgeGroupMapper.selectById(knowledgeUnit.getKnowledgeGroupId());
            KnowledgeGroup group = groupMap.get(groupId);

            if (group != null) {
                // 设置知识组名称
                knowledgeUnit.setKnowledgeGroupName(group.getKnowledgeGroupName());
                // 设置创建用户名称
                knowledgeUnit.setCreateUserName(group.getCreateUserName());
                // 设置更新用户名称
                knowledgeUnit.setUpdateUserName(group.getUpdateUserName());
                // 拼接MinIO URL
                String minIoUrl = minioEndpoint + knowledgeUnit.getOriginalFileDownloadUrl();
                // 设置拼接后的MinIO URL
                knowledgeUnit.setOriginalFileDownloadUrl(minIoUrl);
            }
        }
        return new TableDataInfo(knowledgeUnits, page.getTotal());
    }

    @Override
    public AjaxResult getKnowledgeUnitById(int id) {
        KnowledgeUnit knowledgeUnit = knowledgeUnitMapper.selectById(id);
        // 获取创建人
        SysUser createUser = sysUserMapper.selectUserById(knowledgeUnit.getCreateUserId());
        knowledgeUnit.setCreateUserName(createUser.getUserName());
        if (knowledgeUnit.getUpdateUserId() != null && knowledgeUnit.getUpdateUserId() > 0) {
            // 获取更新人
            SysUser updater = sysUserMapper.selectUserById(knowledgeUnit.getUpdateUserId());
            knowledgeUnit.setUpdateUserName(updater.getUserName());
        }
        // 拼接MinIO URL
        String minIoUrl = minioEndpoint + knowledgeUnit.getOriginalFileDownloadUrl();
        // 设置拼接后的MinIO URL
        knowledgeUnit.setOriginalFileDownloadUrl(minIoUrl);
        return AjaxResult.success(knowledgeUnit);
    }

    @Override
    public AjaxResult updateKnowledgeUnit(KnowledgeUnit knowledgeUnit) {
        try {
            LambdaUpdateWrapper<KnowledgeUnit> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(KnowledgeUnit::getKnowledgeUnitName, knowledgeUnit.getKnowledgeUnitName())
                    .eq(KnowledgeUnit::getId, knowledgeUnit.getId())
                    .set(KnowledgeUnit::getKnowledgeGroupId, knowledgeUnit.getKnowledgeGroupId())
                    .set(KnowledgeUnit::getUpdateTime, LocalDateTime.now())
                    .set(KnowledgeUnit::getUpdateUserId, SecurityUtils.getLoginUser().getUserId());
            int result = knowledgeUnitMapper.update(wrapper);
            if (result == 1){
                return AjaxResult.success("保存/更新成功!");
            }else {
                return AjaxResult.error("保存/更新失败!");
            }
        }catch (Exception e){
            return AjaxResult.error("保存/更新失败!原因"+e.getMessage());
        }
    }

    @Override
    public AjaxResult deleteKnowledgeUnit(int id) {
        // todo 删除知识单元下的全部切片信息

        int result = knowledgeUnitMapper.deleteById(id);
        if (result == 1){
            return AjaxResult.success("删除成功!");
        }else {
            return AjaxResult.error("删除失败!");
        }

    }

    /** {@inheritDoc} */
    @Override
    public Long createFromCompletedImport(KnowledgeImportTask task, ExternalKnowledgeTaskStatus externalStatus)
    {
        ExternalKnowledgeTaskStatus.Result result = externalStatus.getResult();

        KnowledgeUnit knowledgeUnit = new KnowledgeUnit();
        knowledgeUnit.setDocumentId(result.getDocumentId());
        knowledgeUnit.setKnowledgeGroupId(task.getKnowledgeGroupId());
        knowledgeUnit.setKnowledgeUnitName(result.getItemName());
        knowledgeUnit.setOriginalFileName(task.getFileName());
        knowledgeUnit.setFileSha256(result.getFileSha256());
        knowledgeUnit.setOriginalFileDownloadUrl(result.getOriginalFileDownloadUrl());
        knowledgeUnit.setChunkCount(result.getChunkCount());
        knowledgeUnit.setTotalTextLength(result.getTotalTextLength());
        knowledgeUnit.setCreateUserId(task.getCreateUserId());
        knowledgeUnit.setCreateTime(LocalDateTime.now());
        knowledgeUnit.setIsDeleted(0);

        this.save(knowledgeUnit);
        return knowledgeUnit.getId();
    }

}
