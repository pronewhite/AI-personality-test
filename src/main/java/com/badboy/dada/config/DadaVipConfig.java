package com.badboy.dada.config;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/12 11:22
 */
@Configuration
public class DadaVipConfig {

    @Bean
    public Scheduler vipSchedulers() {
        AtomicInteger count = new AtomicInteger(0);
        ThreadFactory threadFactory = (r) -> {
            Thread thread = new Thread(r);
            thread.setName("VIP Thread-" + count.getAndIncrement());
            thread.setDaemon(Boolean.FALSE);
            return thread;
        };
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, threadFactory);
        return Schedulers.from(scheduledExecutorService);
    }
}
