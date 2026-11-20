package com.seeyon.kmp.task;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * kmp-parent
 * <p> File: TestTask.java </p>
 * <p> Description:  (测试定时任务)</p>
 * <p> Date: 2026/11/20 19:20</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
//@Component
public class TestTask {
    //@Scheduled(cron = "0/5 * * * * ?")//每5秒执行一次
    @Scheduled(fixedDelay = 5000)// 在上次调用结束和下一次调用开始之间的固定时间段内执行带注释的方法。
    public void testTask() {
        System.out.println("开始前时间是:"+LocalDateTime.now());
        testTask2();
    }

    public void testTask2() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
