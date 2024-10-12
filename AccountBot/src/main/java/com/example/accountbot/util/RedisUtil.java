package com.example.accountbot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long LOCK_EXPIRE_TIME = 300000; // 5分鐘

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

    // 嘗試獲取鎖
    public boolean tryLock(String lockKey) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            return success != null && success;
        } catch (RedisConnectionFailureException ex) {
            log.error("Failed to connect to Redis to try lock.", ex);
            return false;
        }
    }
}
