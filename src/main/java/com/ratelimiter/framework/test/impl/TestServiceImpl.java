package com.ratelimiter.framework.test.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ratelimiter.framework.aop.RateLimiterMethod;
import com.ratelimiter.framework.aop.RateLimiterType;
import com.ratelimiter.framework.test.TestService;

/**
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
@Service("testService")
public class TestServiceImpl implements TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @RateLimiterMethod(type = RateLimiterType.TOKENBUCKETSTRATEGY, qps = 10, fallBackMethod = "getTestFallBack")
    @Override
    public void getTest() {
        logger.info("get test execute");
    }

    public void getTestFallBack() {
        logger.info("get test fallBack method execute");
    }
}
