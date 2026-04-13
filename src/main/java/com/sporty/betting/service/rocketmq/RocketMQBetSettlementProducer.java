package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
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
        rocketMQTemplate.asyncSend(TOPIC, MessageBuilder.withPayload(message).build(),
                new org.apache.rocketmq.client.producer.SendCallback() {
                    @Override
                    public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                        log.info("Bet settlement sent successfully for bet ID: {} [msgId={}]",
                                message.getBetId(), sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("Failed to send bet settlement for bet ID: {}", message.getBetId(), e);
                    }
                });
    }
}
