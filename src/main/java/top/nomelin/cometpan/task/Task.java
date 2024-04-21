package top.nomelin.cometpan.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Task {
    private final Logger logger = LoggerFactory.getLogger(Task.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public Task(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 60000) // 延迟1秒后执行，然后每隔60秒执行一次
    public void pingRedis() {
        try {
            String pingResult = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().ping();
            logger.info("redis连接成功: " + pingResult);
        } catch (Exception e) {
            logger.warn("redis连接失败: " + e.getMessage());
            pingRedis();//如果ping失败，则立即重新ping
        }
    }
}
