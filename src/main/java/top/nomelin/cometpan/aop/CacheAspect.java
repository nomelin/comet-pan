package top.nomelin.cometpan.aop;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CacheType;
import top.nomelin.cometpan.interfaces.DoubleCache;
import top.nomelin.cometpan.util.Util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * CacheAspect
 *
 * @author nomelin
 * @since 2024/05/19 11:12
 **/
@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class CacheAspect {
    private final Cache<String, Object> cache;
    private final RedisTemplate<String, Object> redisTemplate;

    @Pointcut("@annotation(top.nomelin.cometpan.interfaces.DoubleCache)")
    public void cacheAspect() {
    }

    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        //拼接解析springEl表达式的map
        String[] paramNames = signature.getParameterNames();//获取参数名
        Object[] args = point.getArgs();//获取参数值
        TreeMap<String, Object> treeMap = new TreeMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            treeMap.put(paramNames[i], args[i]);
        }

        DoubleCache annotation = method.getAnnotation(DoubleCache.class);//获取注解
        String elResult = Util.parse(annotation.key(), treeMap);//解析springEl表达式
        String realKey = annotation.cacheName() + Constants.CACHE_COLON + elResult;//拼接缓存key
        Random random = new Random(System.nanoTime());
        long randomTime = random.nextLong(annotation.randomTimeAdded());//随机时间

        //强制更新
        if (annotation.type() == CacheType.PUT) {
            Object object = point.proceed();
            redisTemplate.opsForValue().set(realKey, object, annotation.l2TimeOut() + randomTime, TimeUnit.SECONDS);
            cache.put(realKey, object);
            return object;
        }
        //删除
        else if (annotation.type() == CacheType.DELETE) {
            redisTemplate.delete(realKey);
            cache.invalidate(realKey);
            return point.proceed();
        }
//        printAllCacheItems();

        //读写，查询Caffeine
        Object caffeineCache = cache.getIfPresent(realKey);
        if (Objects.nonNull(caffeineCache)) {
            log.info("从Caffeine中获取缓存:" + realKey);
            return caffeineCache;
        }

        //查询Redis
        Object redisCache = redisTemplate.opsForValue().get(realKey);
        if (Objects.nonNull(redisCache)) {
            log.info("从Redis中获取缓存:" + realKey);
            cache.put(realKey, redisCache);
//            printAllCacheItems();
            return redisCache;
        }

        log.info("缓存不存在，从数据库中查询:" + realKey);
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            //写入Redis
            redisTemplate.opsForValue().set(realKey, object, annotation.l2TimeOut() + randomTime, TimeUnit.SECONDS);
            //写入Caffeine
            cache.put(realKey, object);
        }
        return object;
    }
    public void printAllCacheItems() {
        Map<String, Object> cacheMap = cache.asMap();
        log.info("当前缓存数量:" + cacheMap.size());
        cacheMap.forEach((key, value) -> log.info("Key: " + key + ", Value: " + value));
        log.info("end----------");
    }
}
