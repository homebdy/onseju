package com.onseju.matchingservice.engine.orderbook;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;

import java.util.Comparator;

public class BuyOrderBook extends AbstractOrderBook {

    public BuyOrderBook() {
        super(Comparator.comparing(Price::getValue).reversed()); // 높은 가격 우선
    }

    @Override
    protected boolean isCorrectOrderType(TradeOrder order) {
        return order.isBuyType();
    }

    @Override
    protected boolean isWorseThanTop(TradeOrder order) {
        Price highest = elements.firstKey();
        return !highest.isHigherThan(order.getPrice());
    }
}
