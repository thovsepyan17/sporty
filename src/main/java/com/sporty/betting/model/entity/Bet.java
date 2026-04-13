package com.sporty.betting.model.entity;

import com.sporty.betting.model.enums.BetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "bets", indexes = {
        @Index(name = "idx_bet_event_status", columnList = "eventId, status")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bet {

    @Id
    private String betId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventMarketId;

    @Column(nullable = false)
    private String eventWinnerId;

    @Column(nullable = false)
    private BigDecimal betAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bet bet = (Bet) o;
        return betId != null && betId.equals(bet.betId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(betId);
    }
}
