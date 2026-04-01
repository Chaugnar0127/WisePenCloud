package com.oriole.wisepen.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaErrorHandlerConfiguration {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // 配置死信路由：将失败的消息发送到 "原Topic名称.DLQ"
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(record.topic() + ".DLQ", record.partition()));

        // 配置重试策略：最多重试 3 次，每次间隔 2 秒
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 配置不重试的异常类型（真正的毒消息，直接进 DLQ）
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                MessageConversionException.class,
                JsonProcessingException.class
        );

        return errorHandler;
    }
}
