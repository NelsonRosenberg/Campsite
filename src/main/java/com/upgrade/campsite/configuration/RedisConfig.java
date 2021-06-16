package com.upgrade.campsite.configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void init() {
        try {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .build();
            redisServer.start();
            log.info("Started redis server on port " + String.valueOf(redisPort));
        } catch (Exception ex) {
            log.error("Could not start redis server, port is in use or server already started.");
        }
    }

    @PreDestroy
    public void destroy() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setEnableTransactionSupport(true);//Open transaction support
        return template;
    }

}
