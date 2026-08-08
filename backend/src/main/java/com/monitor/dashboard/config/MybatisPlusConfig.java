package com.monitor.dashboard.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.monitor.dashboard.mapper")
public class MybatisPlusConfig {
}
