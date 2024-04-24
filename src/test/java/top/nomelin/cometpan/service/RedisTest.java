package top.nomelin.cometpan.service;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisService redisService;

    @Test
    public void testRedis() throws InterruptedException {

//        redisService.setValue("hello","world");
//        redisService.expire("hello",10);
//        System.out.println(redisService.getExpire("hello"));
//        Thread.sleep(5000);
//
//        System.out.println(redisService.getExpire("hello"));

    }
}
