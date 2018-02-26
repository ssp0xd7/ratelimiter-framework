package com.ratelimiter.framework.strategy.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import com.google.common.util.concurrent.RateLimiter;
import com.ratelimiter.framework.strategy.RateLimiterStrategy;

/**
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
@Service("tokenBucketStrategy")
public class TokenBucketStrategy extends RateLimiterStrategy {

    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    protected boolean tryAcquire(String key, long qps, long duration) {
        if (duration == 0) {
            return tryAcquire(key, qps);
        }
        RateLimiter rateLimiter = createLimiter(key, qps);
        if(rateLimiter == null){
            return true;
        }
        return rateLimiter.tryAcquire(duration, TimeUnit.SECONDS);
    }

    @Override
    protected boolean tryAcquire(String key, long qps) {
        RateLimiter rateLimiter = createLimiter(key, qps);
        if(rateLimiter == null){
            return true;
        }
        return rateLimiter.tryAcquire();
    }

    /**
     * 构造RateLimiter,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同(SmoothBursty)
     *
     * @param key
     *            相同key返回同一个rateLimiter
     * @param qps
     * @return
     */
    private  RateLimiter createLimiter(String key, long qps) {
        RateLimiter result = limiters.get(key);
        if (result == null) {
            RateLimiter value = RateLimiter.create(qps);
            result = value;
            RateLimiter putByOtherThread = limiters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        return result;
    }
}
