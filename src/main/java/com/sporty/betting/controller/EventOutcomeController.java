package com.sporty.betting.controller;

import com.sporty.betting.model.dto.ApiResponse;
import com.sporty.betting.model.dto.EventOutcomeRequest;
import com.sporty.betting.service.kafka.EventOutcomeProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/event-outcomes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Outcomes", description = "Publish sports event outcomes to trigger bet settlement")
public class EventOutcomeController {

    private final EventOutcomeProducer eventOutcomeProducer;

    @Operation(
            summary = "Publish event outcome",
            description = "Publishes a sports event outcome to the Kafka 'event-outcomes' topic. "
                    + "This triggers asynchronous bet matching and settlement via RocketMQ."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "Event outcome accepted and published to Kafka",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error in request body",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> publishEventOutcome(
            @Valid @RequestBody EventOutcomeRequest request) {
        log.info("REST request to publish event outcome: {}", request);
        eventOutcomeProducer.publish(request);
        return ResponseEntity.accepted()
                .body(ApiResponse.ok("Event outcome published to Kafka successfully"));
    }
}
