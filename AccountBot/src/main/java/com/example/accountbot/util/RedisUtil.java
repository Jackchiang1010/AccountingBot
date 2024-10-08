package com.example.accountbot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean isCacheExist(String key) {
        return redisTemplate.hasKey(key);
    }

    public String getDataFromCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.error("Redis connection failed. Falling back to database.", ex);
            return null;
        }
    }

    public void setDataToCache(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (RedisConnectionFailureException ex) {
            log.error("Failed to connect to Redis to set cache.", ex);
        }
    }

    public void clearCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException ex) {
            log.error("Failed to connect to Redis to clear cache.", ex);
        }
    }
}
