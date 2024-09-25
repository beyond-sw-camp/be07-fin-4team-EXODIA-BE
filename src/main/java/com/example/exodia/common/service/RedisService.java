package com.example.exodia.common.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String RESERVATION_LOCK_PREFIX = "reservation:lock:";

	@Qualifier("documentRedisTemplate")
	private final RedisTemplate<String, Object> documentRedisTemplate;

	@Autowired
	public RedisService(RedisTemplate<String, Object> redisTemplate, @Qualifier("documentRedisTemplate") RedisTemplate<String, Object> documentRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.documentRedisTemplate = documentRedisTemplate;
	}

	public void setValues(String key, String data, Duration duration) {
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		values.set(key, data, duration);
	}

	@Transactional(readOnly = true)
	public String getValues(String key) {
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		if (values.get(key) == null) {
			return "false";
		}
		return (String)values.get(key);
	}

	public void deleteValues(String key) {
		redisTemplate.delete(key);
	}

	public boolean checkExistsValue(String value) {
		// 조회하려는 데이터가 존재하는지
		return !value.equals("false");
	}

	public void setListValue(String key, Long value) {
		ListOperations<String, Object> listValues = documentRedisTemplate.opsForList();
		listValues.leftPush(key, value);
	}

	public List<Object> getListValue(String key) {
		ListOperations<String, Object> listOps = documentRedisTemplate.opsForList();
		Long size = listOps.size(key);
		if (size == null || size == 0) {
			return Collections.emptyList();
		}
		return listOps.range(key, 0, size - 1);
	}
}

