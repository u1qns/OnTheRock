package ontherock.contents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, LocalDateTime> localDateTimeRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, LocalDateTime> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}