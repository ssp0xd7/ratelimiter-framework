package com.ratelimiter.framework.aop;

/**
 * 限流类型枚举
 * 
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
public enum RateLimiterType {
    TOKENBUCKETSTRATEGY, //令牌桶 
    COUNTERSTRATEGY, //计数器（滑动窗口）
    LEAKYBUCKETSTRATEGY;//漏桶
}
