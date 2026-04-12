package com.sporty.betting.model.dto;

import com.sporty.betting.model.enums.BetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetSettlementMessage {

    private String betId;
    private String userId;
    private String eventId;
    private BetStatus result;
    private BigDecimal betAmount;
    private BigDecimal payout;
}
