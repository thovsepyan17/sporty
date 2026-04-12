package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.rocketmq.mock", havingValue = "false", matchIfMissing = true)
public class RocketMQBetSettlementProducer implements BetSettlementProducer {

    private static final String TOPIC = "bet-settlements";

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void send(BetSettlementMessage message) {
        log.info("Sending bet settlement to RocketMQ topic '{}': {}", TOPIC, message);
        rocketMQTemplate.convertAndSend(TOPIC, message);
        log.info("Bet settlement sent successfully for bet ID: {}", message.getBetId());
    }
}
