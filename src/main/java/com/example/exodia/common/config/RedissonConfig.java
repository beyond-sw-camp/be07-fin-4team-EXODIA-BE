package com.example.exodia.common.config;

import jakarta.annotation.PostConstruct;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.nodes}")
    private List<String> redisNodes;

    @PostConstruct
    public void checkRedisNodes() {
        System.out.println("Redis Nodes: " + redisNodes);
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        ClusterServersConfig clusterConfig = config.useClusterServers()
                .setScanInterval(2000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setConnectTimeout(10000)
                .setTimeout(3000);

        redisNodes.forEach(node -> clusterConfig.addNodeAddress("redis://" + node));

        return Redisson.create(config);
    }
}
