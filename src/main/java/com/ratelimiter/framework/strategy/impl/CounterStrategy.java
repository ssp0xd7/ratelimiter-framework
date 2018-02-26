package com.ratelimiter.framework.strategy.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ratelimiter.framework.strategy.RateLimiterStrategy;

/**
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
@Service("counterStrategy")
public class CounterStrategy extends RateLimiterStrategy {

    /**
     * Guava Cache存储计数器，过期时间设置为2秒
     */
    private final ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean tryAcquire(String key, long qps, long duration) throws ExecutionException {
        if (duration == 0) {
            return tryAcquire(key, qps);
        }
        LoadingCache<Long, AtomicLong> counter = createCouter(key);
        long currNanos = System.nanoTime();
        long waitNanos = TimeUnit.SECONDS.toNanos(duration);
        // TODO: 26/02/2018 滑动窗口  
        //long windows = qps > 100 ? 10 : 1;
        do {
            AtomicLong atomicLong = counter.get(System.currentTimeMillis() / 1000);
            if (atomicLong.incrementAndGet() <= qps) {
                return true;
            }
        } while (System.nanoTime() - currNanos <= waitNanos);

        return false;
    }

    @Override
    protected boolean tryAcquire(String key, long qps) throws ExecutionException {
        LoadingCache<Long, AtomicLong> counter = createCouter(key);
        // TODO: 26/02/2018 滑动窗口  
        //long windows = qps > 100 ? 10 : 1;
        AtomicLong atomicLong = counter.get(System.currentTimeMillis() / 1000);
        if (atomicLong.incrementAndGet() <= qps) {
            return true;
        }

        return false;
    }

    /**
     * 构造计数器,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     *
     * @param key
     *            相同key返回同一个计数器
     * @return
     */
    private LoadingCache<Long, AtomicLong> createCouter(String key) {
        LoadingCache<Long, AtomicLong> result = counters.get(key);
        if (result == null) {
            LoadingCache<Long, AtomicLong> value = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS)
                .build(new CacheLoader<Long, AtomicLong>() {
                    @Override
                    public AtomicLong load(Long seconds) {
                        return new AtomicLong(0);
                    }
                });
            result = value;
            LoadingCache<Long, AtomicLong> putByOtherThread = counters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        return result;
    }
}
