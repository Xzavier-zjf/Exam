package com.example.exam.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner databaseConnectionTest(@Autowired JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                logger.info("正在测试数据库连接...");
                String result = jdbcTemplate.queryForObject("SELECT 'Database connection successful' as message", String.class);
                logger.info("数据库连接测试结果: {}", result);
                
                // 测试用户表是否存在
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
                logger.info("用户表中共有 {} 个用户", userCount);
                
                // 检查admin用户是否存在
                Integer adminCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE username = 'admin'", Integer.class);
                logger.info("admin用户存在: {}", adminCount > 0);
                
            } catch (Exception e) {
                logger.error("数据库连接测试失败: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
}
