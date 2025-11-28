package com.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // ← 이것만 추가!
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // 기본 스레드 2개
        executor.setMaxPoolSize(5);       // 최대 스레드 5개
        executor.setQueueCapacity(100);   // 큐 100개
        executor.setThreadNamePrefix("async-interview-"); // 스레드 이름
        executor.initialize();
        return executor;
    }
}