package com.example.dachuang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @Author WHJ
 * @Date 19/10/2023 16:52
 * @Version 1.0 （版本号）
 * @Description:
 */
@Configuration
@ConfigurationProperties(prefix = "threadpool")
public class ExecutePoolConfiguration {
    @Value("${corePoolSize}")
    private int corePoolSize;

    @Value("${maxPoolSize}")
    private int maxPoolSize;

    @Value("${queueCapacity}")
    private int queueCapacity;

    @Value("${keepAliveSeconds}")
    private int keepAliveSeconds;

//    @Bean(name = "threadPoolTaskExecutor")
//    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
//        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
//        pool.setKeepAliveSeconds(keepAliveSeconds);
//        pool.setCorePoolSize(corePoolSize); // 核心线程池数
//        pool.setMaxPoolSize(maxPoolSize); // 最大线程
//        pool.setQueueCapacity(queueCapacity); // 队列容量
//        pool.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 队列满，线程被拒绝执行策略
//        return pool;
//    }
}
