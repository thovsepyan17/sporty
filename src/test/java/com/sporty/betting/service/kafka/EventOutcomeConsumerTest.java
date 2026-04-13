package com.sporty.betting.service.kafka;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.model.dto.EventOutcomeRequest;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import com.sporty.betting.service.rocketmq.BetSettlementProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EventOutcomeConsumerTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private BetSettlementProducer betSettlementProducer;

    @InjectMocks
    private EventOutcomeConsumer consumer;

    @Captor
    private ArgumentCaptor<BetSettlementMessage> messageCaptor;

    @Test
    @DisplayName("Winning bet produces settlement with WON status and 2x payout")
    void consume_winningBet_sendsWonSettlement() {
        Bet bet = Bet.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING)
                .build();

        when(betRepository.findByEventIdAndStatus("EVT-001", BetStatus.PENDING))
                .thenReturn(List.of(bet));

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-001").eventName("Final").eventWinnerId("TEAM-A").build();

        consumer.consume(outcome);

        verify(betSettlementProducer).send(messageCaptor.capture());
        BetSettlementMessage msg = messageCaptor.getValue();

        assertThat(msg.getBetId()).isEqualTo("BET-001");
        assertThat(msg.getResult()).isEqualTo(BetStatus.WON);
        assertThat(msg.getPayout()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Losing bet produces settlement with LOST status and zero payout")
    void consume_losingBet_sendsLostSettlement() {
        Bet bet = Bet.builder()
                .betId("BET-002").userId("USER-002").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-B")
                .betAmount(new BigDecimal("100.00")).status(BetStatus.PENDING)
                .build();

        when(betRepository.findByEventIdAndStatus("EVT-001", BetStatus.PENDING))
                .thenReturn(List.of(bet));

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-001").eventName("Final").eventWinnerId("TEAM-A").build();

        consumer.consume(outcome);

        verify(betSettlementProducer).send(messageCaptor.capture());
        BetSettlementMessage msg = messageCaptor.getValue();

        assertThat(msg.getBetId()).isEqualTo("BET-002");
        assertThat(msg.getResult()).isEqualTo(BetStatus.LOST);
        assertThat(msg.getPayout()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Multiple bets for same event are each settled correctly")
    void consume_multipleBets_settlesEach() {
        Bet winner = Bet.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING).build();
        Bet loser = Bet.builder()
                .betId("BET-002").userId("USER-002").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-B")
                .betAmount(new BigDecimal("100.00")).status(BetStatus.PENDING).build();

        when(betRepository.findByEventIdAndStatus("EVT-001", BetStatus.PENDING))
                .thenReturn(List.of(winner, loser));

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-001").eventName("Final").eventWinnerId("TEAM-A").build();

        consumer.consume(outcome);

        verify(betSettlementProducer, times(2)).send(messageCaptor.capture());
        List<BetSettlementMessage> messages = messageCaptor.getAllValues();

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getResult()).isEqualTo(BetStatus.WON);
        assertThat(messages.get(1).getResult()).isEqualTo(BetStatus.LOST);
    }

    @Test
    @DisplayName("No pending bets found — no settlement messages sent")
    void consume_noPendingBets_doesNothing() {
        when(betRepository.findByEventIdAndStatus(eq("EVT-999"), eq(BetStatus.PENDING)))
                .thenReturn(Collections.emptyList());

        EventOutcomeRequest outcome = EventOutcomeRequest.builder()
                .eventId("EVT-999").eventName("Unknown").eventWinnerId("NOBODY").build();

        consumer.consume(outcome);

        verify(betSettlementProducer, never()).send(any());
    }
}
