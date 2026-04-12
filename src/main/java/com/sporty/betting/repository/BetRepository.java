package com.sporty.betting.repository;

import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, String> {

    List<Bet> findByEventIdAndStatus(String eventId, BetStatus status);
}
