package com.seeyon.kmp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;

/**
 * kmp-parent
 * <p> File: knowledgeGroupConfig.java </p>
 * <p> Description:  ()</p>
 * <p> Date: 2026/11/17 19:06</p>
 *
 * @author wangLei
 * @version 1.0 （代码版本）
 * @since jdk17（jdk版本）
 */
@MapperScan("com.seeyon.kmp.mapper")
@SpringBootConfiguration
public class knowledgeGroupConfig {
}
