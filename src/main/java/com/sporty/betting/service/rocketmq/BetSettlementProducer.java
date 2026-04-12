package com.sporty.betting.service.rocketmq;

import com.sporty.betting.model.dto.BetSettlementMessage;

public interface BetSettlementProducer {

    void send(BetSettlementMessage message);
}
