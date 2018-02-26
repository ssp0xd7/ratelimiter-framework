package com.ratelimiter.framework;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.ratelimiter.framework.test.TestService;

/**
 * @author kevin(ssp0xd7 @ gmail.com) 26/02/2018
 */
public class RateLimitTest extends TestBase {

    @Autowired
    private TestService testService;

    @Test
    public void tokenBucketTest() throws InterruptedException {
        List<Runnable> tasks = Lists.newArrayList();
        int size = 20;
        final CountDownLatch latch = new CountDownLatch(20);
        for (int i = 0; i < size; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    testService.getTest();
                    latch.countDown();
                }
            };
            tasks.add(runnable);
        }

        ExecutorService threads = Executors.newFixedThreadPool(tasks.size());
        for (Runnable task: tasks) {
            threads.submit(task);
        }
        latch.await();
    }
}
