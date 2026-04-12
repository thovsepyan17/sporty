package com.sporty.betting.init;

import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final BetRepository betRepository;

    @Override
    public void run(String... args) {
        List<Bet> sampleBets = List.of(
                Bet.builder()
                        .betId("BET-001").userId("USER-001").eventId("EVT-001")
                        .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                        .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING).build(),
                Bet.builder()
                        .betId("BET-002").userId("USER-002").eventId("EVT-001")
                        .eventMarketId("MKT-001").eventWinnerId("TEAM-B")
                        .betAmount(new BigDecimal("100.00")).status(BetStatus.PENDING).build(),
                Bet.builder()
                        .betId("BET-003").userId("USER-003").eventId("EVT-002")
                        .eventMarketId("MKT-002").eventWinnerId("TEAM-C")
                        .betAmount(new BigDecimal("75.00")).status(BetStatus.PENDING).build(),
                Bet.builder()
                        .betId("BET-004").userId("USER-001").eventId("EVT-002")
                        .eventMarketId("MKT-002").eventWinnerId("TEAM-D")
                        .betAmount(new BigDecimal("200.00")).status(BetStatus.PENDING).build(),
                Bet.builder()
                        .betId("BET-005").userId("USER-004").eventId("EVT-003")
                        .eventMarketId("MKT-003").eventWinnerId("PLAYER-X")
                        .betAmount(new BigDecimal("150.00")).status(BetStatus.PENDING).build()
        );

        betRepository.saveAll(sampleBets);
        log.info("Initialized {} sample bets in database", sampleBets.size());
        sampleBets.forEach(bet -> log.debug("  {} — event={}, winnerId={}, amount={}",
                bet.getBetId(), bet.getEventId(), bet.getEventWinnerId(), bet.getBetAmount()));
    }
}
