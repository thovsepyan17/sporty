package com.sporty.betting.service;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class BetSettlementServiceTest {

    @Mock
    private BetRepository betRepository;

    @InjectMocks
    private BetSettlementService service;

    @Test
    @DisplayName("settleBet updates bet status to WON and persists")
    void settleBet_won_updatesStatus() {
        Bet bet = Bet.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING)
                .build();

        when(betRepository.findById("BET-001")).thenReturn(Optional.of(bet));

        BetSettlementMessage message = BetSettlementMessage.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .result(BetStatus.WON).betAmount(new BigDecimal("50.00"))
                .payout(new BigDecimal("100.00"))
                .build();

        service.settleBet(message);

        assertThat(bet.getStatus()).isEqualTo(BetStatus.WON);
        verify(betRepository).save(bet);
    }

    @Test
    @DisplayName("settleBet updates bet status to LOST and persists")
    void settleBet_lost_updatesStatus() {
        Bet bet = Bet.builder()
                .betId("BET-002").userId("USER-002").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-B")
                .betAmount(new BigDecimal("100.00")).status(BetStatus.PENDING)
                .build();

        when(betRepository.findById("BET-002")).thenReturn(Optional.of(bet));

        BetSettlementMessage message = BetSettlementMessage.builder()
                .betId("BET-002").userId("USER-002").eventId("EVT-001")
                .result(BetStatus.LOST).betAmount(new BigDecimal("100.00"))
                .payout(BigDecimal.ZERO)
                .build();

        service.settleBet(message);

        assertThat(bet.getStatus()).isEqualTo(BetStatus.LOST);
        verify(betRepository).save(bet);
    }

    @Test
    @DisplayName("settleBet throws when bet not found")
    void settleBet_betNotFound_throws() {
        when(betRepository.findById("BET-999")).thenReturn(Optional.empty());

        BetSettlementMessage message = BetSettlementMessage.builder()
                .betId("BET-999").userId("USER-001").eventId("EVT-001")
                .result(BetStatus.WON).betAmount(new BigDecimal("50.00"))
                .payout(new BigDecimal("100.00"))
                .build();

        assertThatThrownBy(() -> service.settleBet(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BET-999");
    }
}
