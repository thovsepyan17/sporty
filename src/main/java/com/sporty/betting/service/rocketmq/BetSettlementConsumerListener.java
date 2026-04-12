package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.service.BetSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rocketmq.mock", havingValue = "false", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "bet-settlements",
        consumerGroup = "bet-settlement-consumer-group"
)
public class BetSettlementConsumerListener implements RocketMQListener<BetSettlementMessage> {

    private final BetSettlementService betSettlementService;

    @Override
    public void onMessage(BetSettlementMessage message) {
        log.info("Received bet settlement from RocketMQ: {}", message);
        betSettlementService.settleBet(message);
    }
}
