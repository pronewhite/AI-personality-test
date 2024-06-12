package com.badboy.dada.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/11 11:23
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissionConfig {

    private String host;
    private int port;
//    private String password;
    private int database;

    @Bean
    public RedissonClient getRedission() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
//                .setPassword(password)
                .setDatabase(database);
        return org.redisson.Redisson.create(config);
    }
}
