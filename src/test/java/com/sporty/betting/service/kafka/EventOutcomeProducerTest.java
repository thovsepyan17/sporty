package com.sporty.betting.service.kafka;

import com.sporty.betting.model.dto.EventOutcomeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EventOutcomeProducerTest {

    @Mock
    private KafkaTemplate<String, EventOutcomeRequest> kafkaTemplate;

    @InjectMocks
    private EventOutcomeProducer producer;

    @Test
    @DisplayName("publish sends message to 'event-outcomes' topic with eventId as key")
    void publish_sendsToCorrectTopic() {
        EventOutcomeRequest request = EventOutcomeRequest.builder()
                .eventId("EVT-001").eventName("Final").eventWinnerId("TEAM-A").build();

        CompletableFuture<SendResult<String, EventOutcomeRequest>> future = new CompletableFuture<>();
        when(kafkaTemplate.send("event-outcomes", "EVT-001", request)).thenReturn(future);

        producer.publish(request);

        verify(kafkaTemplate).send(eq("event-outcomes"), eq("EVT-001"), eq(request));
    }
}
