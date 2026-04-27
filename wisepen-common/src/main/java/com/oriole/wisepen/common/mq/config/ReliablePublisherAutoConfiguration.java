package com.oriole.wisepen.common.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.common.mq.RedisBackloggedPublisher;
import com.oriole.wisepen.common.mq.ReliablePublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass({KafkaTemplate.class, RedisTemplate.class})
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class ReliablePublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReliablePublisher reliablePublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        return new RedisBackloggedPublisher(kafkaTemplate, redisTemplate, objectMapper);
    }
}
