package com.ratelimiter.framework.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ratelimiter.framework.strategy.RateLimiterStrategy;

/**
 * 限流处理切面类
 * 
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
@Aspect
@Component
@Order(1)
public class RateLimiterAspect {

    @Autowired
    private RateLimiterStrategy tokenBucketStrategy;

    @Autowired
    private RateLimiterStrategy counterStrategy;

    @Autowired
    private RateLimiterStrategy leakyBucketStrategy;

    @Around("@annotation(rateLimiterMethod)")
    public Object method(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        Object result;
        switch (rateLimiterMethod.type()) {
            case TOKENBUCKETSTRATEGY:
                result = tokenBucketStrategy.handle(pjp, rateLimiterMethod);
                break;
            case COUNTERSTRATEGY:
                result = counterStrategy.handle(pjp, rateLimiterMethod);
                break;
            case LEAKYBUCKETSTRATEGY:
                result = leakyBucketStrategy.handle(pjp, rateLimiterMethod);
                break;
            default:
                result = tokenBucketStrategy.handle(pjp, rateLimiterMethod);
        }
        return result;
    }
}
