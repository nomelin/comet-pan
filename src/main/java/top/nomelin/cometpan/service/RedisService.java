package top.nomelin.cometpan.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis基本操作
 *
 * @author nomelin
 */
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取值
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除key
     */

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys 键列表
     */
    public void deleteKey(String... keys) {
        redisTemplate.delete(List.of(keys));
    }

    /**
     * 设置key的过期时间
     *
     * @param key  键
     * @param time 过期时间，单位秒
     */
    public void expire(String key, long time) {
        redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }


    /**
     * 获取key的过期时间
     *
     * @param key 键
     * @return 过期时间，单位秒，-1表示永不过期，-2表示key不存在
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        if (expire == null) {
            return -2;// -1表示永不过期，-2表示key不存在
        }
        return expire;

    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


}
