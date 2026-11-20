package com.seeyon.kmp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootConfiguration
@MapperScan("com.seeyon.kmp.mapper")
@EnableScheduling//开启定时任务
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor pagination =
                new PaginationInnerInterceptor(DbType.MYSQL);

        interceptor.addInnerInterceptor(pagination);

        return interceptor;
    }
}