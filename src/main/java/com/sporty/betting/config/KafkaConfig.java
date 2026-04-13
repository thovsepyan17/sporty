package com.sporty.betting.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public NewTopic eventOutcomesTopic() {
        return TopicBuilder.name("event-outcomes")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, exception) ->
                        log.error("Exhausted retries for record on topic={}, partition={}, offset={}: {}",
                                record.topic(), record.partition(), record.offset(),
                                exception.getMessage()),
                new FixedBackOff(1000L, 3)
        );
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
