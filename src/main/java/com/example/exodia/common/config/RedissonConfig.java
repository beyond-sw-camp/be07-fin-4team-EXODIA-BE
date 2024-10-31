package com.example.exodia.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;  // Redis 클러스터 엔드포인트

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 클러스터 서버 설정
        config.useClusterServers()
                .addNodeAddress("redis://" + redisHost + ":" + redisPort)
                .setScanInterval(2000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setConnectTimeout(10000)
                .setTimeout(3000);

        return Redisson.create(config);
    }
}
