package com.seeyon.kmp.task;

import com.seeyon.kmp.service.KnowledgeImportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * kmp-parent
 * <p> File: KnowledgePollingTask.java </p>
 * <p> Description:  (定时查询导入进度)</p>
 * <p> Date: 2026/11/20 19:46</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@Component
@Slf4j
public class KnowledgePollingTask {
    @Autowired
    private KnowledgeImportTaskService knowledgeImportTaskService;

    @Scheduled(fixedRate = 5000) // 每5秒执行一次,也能写成配置文件的形式(修改不用重启服务器),如下
    @Scheduled(fixedDelayString = "#{@kmpConfig.knowledgeSystemRestClient}")
    public void knowledgePollingTask() {
        log.info("开始查询导入进度");
        knowledgeImportTaskService.pollProcessingTasks();
        log.info("定时任务执行完成...");
    }
}
