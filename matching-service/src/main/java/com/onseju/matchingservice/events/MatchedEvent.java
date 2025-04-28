package com.onseju.matchingservice.events;

import com.onseju.matchingservice.domain.TradeOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchedEvent(
        UUID id,
        String companyCode,
        Long buyOrderId,
        Long buyAccountId,
        Long sellOrderId,
        Long sellAccountId,
        BigDecimal quantity,
        BigDecimal price,
        Long tradeAt
) {

    public static MatchedEvent of(
            TradeOrder incomingOrder,
            TradeOrder existingOrder,
            BigDecimal matchedQuantity
    ) {

        if (incomingOrder.isSellType()) {
            return toMatchedEvent(existingOrder, incomingOrder, matchedQuantity);
        }
        return toMatchedEvent(incomingOrder, existingOrder, matchedQuantity);
    }

    private static MatchedEvent toMatchedEvent(
            TradeOrder buyOrder,
            TradeOrder sellOrder,
            BigDecimal matchedQuantity
    ) {
        return new MatchedEvent(
                UUID.randomUUID(),
                buyOrder.getCompanyCodeValue(),
                buyOrder.getId(),
                buyOrder.getAccountId(),
                sellOrder.getId(),
                sellOrder.getAccountId(),
                matchedQuantity,
                buyOrder.calculatePrice(sellOrder),
                Instant.now().toEpochMilli()
        );
    }
}
