package com.sporty.betting.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.model.dto.EventOutcomeRequest;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"event-outcomes"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("integration")
class BettingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BetRepository betRepository;

    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
    }

    @Test
    @DisplayName("Full flow: POST event outcome -> Kafka -> bet matching -> settlement (mock RocketMQ)")
    void fullSettlementFlow() throws Exception {
        betRepository.saveAll(List.of(
                Bet.builder()
                        .betId("BET-001").userId("USER-001").eventId("EVT-001")
                        .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                        .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING).build(),
                Bet.builder()
                        .betId("BET-002").userId("USER-002").eventId("EVT-001")
                        .eventMarketId("MKT-001").eventWinnerId("TEAM-B")
                        .betAmount(new BigDecimal("100.00")).status(BetStatus.PENDING).build()
        ));

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-001")
                .eventName("Champions League Final")
                .eventWinnerId("TEAM-A")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outcome)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            Bet bet1 = betRepository.findById("BET-001").orElseThrow();
            Bet bet2 = betRepository.findById("BET-002").orElseThrow();
            assertThat(bet1.getStatus()).isEqualTo(BetStatus.WON);
            assertThat(bet2.getStatus()).isEqualTo(BetStatus.LOST);
        });
    }

    @Test
    @DisplayName("Event outcome with no matching bets leaves database unchanged")
    void noMatchingBets_nothingSettled() throws Exception {
        Bet unrelatedBet = Bet.builder()
                .betId("BET-010").userId("USER-005").eventId("EVT-099")
                .eventMarketId("MKT-099").eventWinnerId("TEAM-Z")
                .betAmount(new BigDecimal("200.00")).status(BetStatus.PENDING).build();
        betRepository.save(unrelatedBet);

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-001")
                .eventName("Some Match")
                .eventWinnerId("TEAM-A")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outcome)))
                .andExpect(status().isAccepted());

        Thread.sleep(3000);

        Bet unchanged = betRepository.findById("BET-010").orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(BetStatus.PENDING);
    }

    @Test
    @DisplayName("GET /api/v1/bets returns bets with updated status after settlement")
    void getBets_reflectsSettlement() throws Exception {
        betRepository.save(Bet.builder()
                .betId("BET-100").userId("USER-001").eventId("EVT-050")
                .eventMarketId("MKT-050").eventWinnerId("PLAYER-X")
                .betAmount(new BigDecimal("75.00")).status(BetStatus.PENDING).build());

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-050")
                .eventName("Tennis Final")
                .eventWinnerId("PLAYER-X")
                .build();

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outcome)))
                .andExpect(status().isAccepted());

        await().atMost(10, SECONDS).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/bets/BET-100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("WON"));
        });
    }
}
