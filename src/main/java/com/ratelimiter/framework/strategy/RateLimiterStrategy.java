package com.ratelimiter.framework.strategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ratelimiter.framework.aop.RateLimiterMethod;
import com.ratelimiter.framework.utils.AOPUtils;
import com.ratelimiter.framework.utils.KeyFactory;

/**
 * 限流策略抽象类
 * 
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
public abstract class RateLimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterStrategy.class);

    /**
     * 限流处理入口
     * 
     * @param pjp
     * @param rateLimiterMethod
     * @return
     */
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createKey(pjp, rateLimiterMethod.key());
        boolean acquire = tryAcquire(key, rateLimiterMethod.qps(), rateLimiterMethod.duration());
        if (acquire) {
            return pjp.proceed();
        }
        fallBack(pjp, rateLimiterMethod.fallBackMethod());
        return null;
    }

    /**
     * 阻塞获取请求权限
     *
     * @param key
     *            请求标识key
     * @param qps
     * @param duration
     *            阻塞时间（秒）
     * @return
     */
    protected abstract boolean tryAcquire(String key, long qps, long duration) throws ExecutionException;

    /**
     * 获取请求权限
     *
     * @param key
     *            请求标识key
     * @param qps
     * @return
     */
    protected abstract boolean tryAcquire(String key, long qps) throws ExecutionException;

    /**
     * 构造key
     *
     * @param pjp
     * @param limiterKey
     * @return
     */
    private String createKey(ProceedingJoinPoint pjp, String limiterKey) {
        //使用注解时指定了key
        if (StringUtils.isNotBlank(limiterKey)) {
            return limiterKey;
        }
        return KeyFactory.createKey(pjp);
    }

    /**
     * 执行降级
     * 
     * @param pjp
     * @param fallBackMethod
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object fallBack(ProceedingJoinPoint pjp, String fallBackMethod)
        throws InvocationTargetException, IllegalAccessException {
        //被限流了,如果设置了降级方法，则执行降级方法
        if (StringUtils.isNotBlank(fallBackMethod)) {
            Object obj = pjp.getTarget();
            Method method = AOPUtils.getMethodFromTarget(pjp, fallBackMethod);
            if (method != null) {
                Object result = method.invoke(obj, pjp.getArgs());
                logger.info("fallBack method executed,class:{},method:{}", obj.getClass().getName(), fallBackMethod);
                return result;
            }
            logger.warn("fallBack method not exist,class:{},method:{}", obj.getClass().getName(), fallBackMethod);
        }
        return null;
    }
}
