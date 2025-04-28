package com.onseju.matchingservice.engine.orderbook;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;

import java.util.Comparator;

/**
 * SellOrderBook: 매도 주문장
 * 매도 주문을 관리 및 매수 주문과 매칭
 */
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
