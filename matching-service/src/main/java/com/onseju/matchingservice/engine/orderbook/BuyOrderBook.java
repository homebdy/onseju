package com.onseju.matchingservice.engine.orderbook;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;

import java.util.Comparator;


/**
 * BuyOrderBook: 매수 주문장
 * 매수 주문을 관리 및 매도 주문과 매칭
 */
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
