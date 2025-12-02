package com.novaerp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Defaults to spring.redis.* from application.yml
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, String> rt = new RedisTemplate<>();
        rt.setConnectionFactory(cf);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new StringRedisSerializer());
        return rt;
    }
}
