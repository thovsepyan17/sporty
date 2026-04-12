package com.sporty.betting.service.kafka;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.model.dto.EventOutcomeRequest;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import com.sporty.betting.service.rocketmq.BetSettlementProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventOutcomeConsumer {

    private static final BigDecimal WIN_MULTIPLIER = new BigDecimal("2.00");

    private final BetRepository betRepository;
    private final BetSettlementProducer betSettlementProducer;

    @KafkaListener(topics = "event-outcomes", groupId = "betting-service")
    public void consume(EventOutcomeRequest eventOutcome) {
        log.info("Received event outcome from Kafka: {}", eventOutcome);

        List<Bet> pendingBets = betRepository.findByEventIdAndStatus(
                eventOutcome.getEventId(), BetStatus.PENDING);

        if (pendingBets.isEmpty()) {
            log.info("No pending bets found for event ID: {}", eventOutcome.getEventId());
            return;
        }

        log.info("Found {} pending bet(s) for event ID: {}",
                pendingBets.size(), eventOutcome.getEventId());

        for (Bet bet : pendingBets) {
            boolean isWinner = bet.getEventWinnerId().equals(eventOutcome.getEventWinnerId());
            BetStatus result = isWinner ? BetStatus.WON : BetStatus.LOST;
            BigDecimal payout = isWinner
                    ? bet.getBetAmount().multiply(WIN_MULTIPLIER)
                    : BigDecimal.ZERO;

            BetSettlementMessage message = BetSettlementMessage.builder()
                    .betId(bet.getBetId())
                    .userId(bet.getUserId())
                    .eventId(bet.getEventId())
                    .result(result)
                    .betAmount(bet.getBetAmount())
                    .payout(payout)
                    .build();

            betSettlementProducer.send(message);
        }
    }
}
