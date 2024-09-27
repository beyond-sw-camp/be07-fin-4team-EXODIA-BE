package com.example.exodia.common.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    public LettuceConnectionFactory redisConnectionFactory(int index) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(index);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Primary
    LettuceConnectionFactory connectionFactory() {
        return redisConnectionFactory(0);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }


    @Bean
    @Qualifier("videoRoomConnectionFactory")
    LettuceConnectionFactory videoRoomConnectionFactory() {
        return redisConnectionFactory(1);
    }

    @Bean
    @Qualifier("videoRoomRedisTemplate")
    public RedisTemplate<String, Object> videoRoomRedisTemplate(@Qualifier("videoRoomConnectionFactory") LettuceConnectionFactory videoRoomConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(videoRoomConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return redisTemplate;
    }


    @Bean
    public ChannelTopic roomEventsTopic() {
        return new ChannelTopic("room-events");
    }

    @Bean
    public ChannelTopic userEventsTopic() {
        return new ChannelTopic("user-events");
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisMessageSubscriber redisMessageSubscriber) {
        return new MessageListenerAdapter(redisMessageSubscriber);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            MessageListenerAdapter messageListenerAdapter,
            RedisConnectionFactory redisConnectionFactory,
            ChannelTopic roomEventsTopic,
            ChannelTopic userEventsTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, roomEventsTopic);
        container.addMessageListener(messageListenerAdapter, userEventsTopic);
        return container;
    }

    // 최근 조회 문서
    @Bean
    @Qualifier("7")
    LettuceConnectionFactory connectionFactoryViewdDoc() {
        return redisConnectionFactory(6);
    }

    @Bean
    @Qualifier("7")
    public RedisTemplate<String, Object> docViewdRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactoryViewdDoc());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    // 최근 업데이트 문서
    @Bean
    @Qualifier("8")
    LettuceConnectionFactory connectionFactoryUpdatedDoc() {
        return redisConnectionFactory(7);
    }

    @Bean
    @Qualifier("8")
    public RedisTemplate<String, Object> docUpdatedRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactoryUpdatedDoc());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    // 조회수
    @Bean
    @Qualifier("hits")
    LettuceConnectionFactory connectionFactoryHits() {
        return redisConnectionFactory(10);
    }

    @Bean
    @Qualifier("hits")
    public RedisTemplate<String, Object> hitsRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactoryUpdatedDoc());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }


    // ShedLock을 위한 Redis 설정
    @Bean
    @Qualifier("10")
    public LettuceConnectionFactory shedlockConnectionFactory() {
        return redisConnectionFactory(9);
    }

    @Bean
    @Qualifier("10")
    public RedisTemplate<String, Object> shedlockRedisTemplate(@Qualifier("10") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public LockProvider lockProvider(@Qualifier("10") RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "shedlock");
    }



}
