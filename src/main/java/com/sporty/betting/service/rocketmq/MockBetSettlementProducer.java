package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.service.BetSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock implementation used when RocketMQ infrastructure is not available.
 * Logs the payload and directly triggers bet settlement to simulate the full flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rocketmq.mock", havingValue = "true")
public class MockBetSettlementProducer implements BetSettlementProducer {

    private final BetSettlementService betSettlementService;

    @Override
    public void send(BetSettlementMessage message) {
        log.info("[MOCK RocketMQ] Payload for 'bet-settlements': {}", message);
        betSettlementService.settleBet(message);
    }
}
