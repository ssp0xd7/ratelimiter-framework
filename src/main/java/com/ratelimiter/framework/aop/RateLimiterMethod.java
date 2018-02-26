package com.ratelimiter.framework.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiterMethod {

    /**
     * 限流算法类型,默认采用令牌桶
     *
     * @return
     */
    RateLimiterType type() default RateLimiterType.TOKENBUCKETSTRATEGY;

    /**
     * 限流的QPS值,必填
     * 
     * @return
     */
    long qps();

    /**
     * 降级方法(方法签名，反射)
     * 
     * @return
     */
    String fallBackMethod() default "";

    /**
     * 请求key标识,key相同的情况下则使用同一个RateLimiter
     * 
     * @return
     */
    String key() default "";

    /**
     * 阻塞等待时间
     * 
     * @return
     */
    long duration() default 0;
}
