package top.nomelin.cometpan.interfaces;

import top.nomelin.cometpan.common.enums.CacheType;

import java.lang.annotation.*;

/**
 * DoubleCache
 *
 * @author nomelin
 * @since 2024/05/19 10:50
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleCache {
    String cacheName();
    String key(); //支持springEl表达式
    long l2TimeOut() default 120;
    CacheType type() default CacheType.FULL;

    /**
     * 随机时间添加，防止缓存雪崩(会随机添加不超过randomTimeAdded秒的随机时间)
     */
    long randomTimeAdded() default 5;
}
