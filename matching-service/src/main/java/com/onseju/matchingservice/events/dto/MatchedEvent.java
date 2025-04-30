package com.onseju.matchingservice.events.dto;

import com.onseju.matchingservice.domain.TradeOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchedEvent(
        UUID id,
        String companyCode,
        Long buyOrderId,
        Long buyMemberId,
        Long sellOrderId,
        Long sellMemberId,
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
                buyOrder.getMemberId(),
                sellOrder.getId(),
                sellOrder.getMemberId(),
                matchedQuantity,
                buyOrder.calculatePrice(sellOrder),
                Instant.now().toEpochMilli()
        );
    }
}
