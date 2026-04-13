package com.sporty.betting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.model.dto.EventOutcomeRequest;
import com.sporty.betting.service.kafka.EventOutcomeProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventOutcomeController.class)
@Tag("unit")
class EventOutcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventOutcomeProducer eventOutcomeProducer;

    @Test
    @DisplayName("POST /api/v1/event-outcomes — valid request returns 202 and publishes to Kafka")
    void publishEventOutcome_validRequest_returns202() throws Exception {
        EventOutcomeRequest request = EventOutcomeRequest.builder()
                .eventId("EVT-001")
                .eventName("Champions League Final")
                .eventWinnerId("TEAM-A")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event outcome published to Kafka successfully"));

        verify(eventOutcomeProducer).publish(any(EventOutcomeRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/event-outcomes — missing eventId returns 400")
    void publishEventOutcome_missingEventId_returns400() throws Exception {
        EventOutcomeRequest request = EventOutcomeRequest.builder()
                .eventName("Champions League Final")
                .eventWinnerId("TEAM-A")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eventOutcomeProducer, never()).publish(any());
    }

    @Test
    @DisplayName("POST /api/v1/event-outcomes — missing eventWinnerId returns 400")
    void publishEventOutcome_missingWinnerId_returns400() throws Exception {
        EventOutcomeRequest request = EventOutcomeRequest.builder()
                .eventId("EVT-001")
                .eventName("Champions League Final")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eventOutcomeProducer, never()).publish(any());
    }

    @Test
    @DisplayName("POST /api/v1/event-outcomes — empty body returns 400")
    void publishEventOutcome_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eventOutcomeProducer, never()).publish(any());
    }
}
