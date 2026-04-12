package com.sporty.betting.service;

import com.sporty.betting.model.dto.BetSettlementMessage;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetSettlementService {

    private final BetRepository betRepository;

    @Transactional
    public void settleBet(BetSettlementMessage message) {
        Bet bet = betRepository.findById(message.getBetId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bet not found: " + message.getBetId()));

        bet.setStatus(message.getResult());
        betRepository.save(bet);

        log.info("Bet {} settled: user={}, result={}, payout={}",
                message.getBetId(), message.getUserId(),
                message.getResult(), message.getPayout());
    }
}
