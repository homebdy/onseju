package com.onseju.matchingservice.engine.orderbook;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;

import java.util.Comparator;

public class SellOrderBook extends AbstractOrderBook {

    public SellOrderBook() {
        super(Comparator.comparing(Price::getValue)); // 낮은 가격 우선
    }

    @Override
    protected boolean isCorrectOrderType(TradeOrder order) {
        return order.isSellType();
    }

    @Override
    protected boolean isWorseThanTop(TradeOrder order) {
        Price lowest = elements.firstKey();
        return lowest.isHigherThan(order.getPrice());
    }
}
