package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.service.BetSettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MockBetSettlementProducerTest {

    @Mock
    private BetSettlementService betSettlementService;

    @InjectMocks
    private MockBetSettlementProducer producer;

    @Test
    @DisplayName("send delegates directly to BetSettlementService")
    void send_delegatesToSettlementService() {
        BetSettlementMessage message = BetSettlementMessage.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .result(BetStatus.WON).betAmount(new BigDecimal("50.00"))
                .payout(new BigDecimal("100.00"))
                .build();

        producer.send(message);

        verify(betSettlementService).settleBet(message);
    }
}
