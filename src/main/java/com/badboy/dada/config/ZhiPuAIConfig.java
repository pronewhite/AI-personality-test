package com.badboy.dada.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/8 12:56
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "zhipuai")
public class ZhiPuAIConfig {

    private String apiKey;

    @Bean
    public ClientV4 getZhipuAiClient(){
        return new ClientV4.Builder(apiKey).build();
    }
}
