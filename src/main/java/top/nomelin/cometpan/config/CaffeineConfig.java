package top.nomelin.cometpan.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CaffeineConfig
 *
 * @author nomelin
 * @since 2024/05/19 10:30
 **/
@Configuration
public class CaffeineConfig {
    @Value("${cache.l1.max-size:1024}")
    private int MAX_SIZE;
    @Value("${cache.l1.init-size:128}")
    private int INITIAL_SIZE;
    @Value("${cache.l1.expire-time:60}")
    private int L1_TIMEOUT;

    @Bean
    public Cache<String, Object> caffeineCache() {
        //Caffeine有4种缓存淘汰设置
        //大小 （LFU算法进行淘汰）
        //权重 （大小与权重 只能二选一）
        //时间
        //引用
        return Caffeine.newBuilder()
                .initialCapacity(INITIAL_SIZE)//初始大小
                .maximumSize(MAX_SIZE)//最大数量
//                .expireAfterWrite(L1_TIMEOUT, TimeUnit.SECONDS)//某个数据在多久没有被更新后，就过期。
                .expireAfterAccess(L1_TIMEOUT, TimeUnit.SECONDS)//某个数据在多久没有被访问后，就过期。
//                .weakKeys()//弱引用key(不要使用，可能会导致同一个key被保存多次)
                .weakValues()//弱引用value
                .build();
    }
}
