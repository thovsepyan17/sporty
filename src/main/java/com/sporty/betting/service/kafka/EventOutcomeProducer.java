package com.sporty.betting.service.kafka;

import com.sporty.betting.model.dto.EventOutcomeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventOutcomeProducer {

    private static final String TOPIC = "event-outcomes";

    private final KafkaTemplate<String, EventOutcomeRequest> kafkaTemplate;

    public void publish(EventOutcomeRequest eventOutcome) {
        log.info("Publishing event outcome to Kafka topic '{}': {}", TOPIC, eventOutcome);

        kafkaTemplate.send(TOPIC, eventOutcome.getEventId(), eventOutcome)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event outcome: {}", ex.getMessage(), ex);
                    } else {
                        log.info("Event outcome published successfully — partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
