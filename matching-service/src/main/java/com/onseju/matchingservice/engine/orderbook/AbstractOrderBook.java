package com.onseju.matchingservice.engine.orderbook;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.OrderStorage;
import com.onseju.matchingservice.events.MatchedEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * OrderBook: 주식 시장의 모든 매도 및 매수 주문을 저장하는 저장소
 * 매수/매도 주문 호가창의 공통 로직을 담당하는 추상 클래스.
 */
public abstract class AbstractOrderBook {

    /**
     * 가격을 기준으로 정렬된 주문 저장소
     * Comparator는 하위 클래스에서 매수/매도 기준에 따라 전달
     */
    protected final ConcurrentSkipListMap<Price, OrderStorage> elements;

    /**
     * Comparator에 따라 정렬되는 주문 저장소를 초기화.
     *
     * @param comparator 가격 정렬 기준 (매도: 낮은 가격 우선, 매수: 높은 가격 우선)
     */
    protected AbstractOrderBook(Comparator<Price> comparator) {
        this.elements = new ConcurrentSkipListMap<>(comparator);
    }

    /**
     * 주문을 처리하여 매칭 결과를 반환.
     * 시장가/지정가 여부에 따라 분기 처리.
     */
    public Collection<MatchedEvent> matchOrder(final TradeOrder order) {
        if (order.isMarketOrder()) {
            return processMarketOrder(order);
        }
        return processLimitOrder(order);
    }

    // 시장가 주문 처리: 현재 호가창에서 매칭 가능한 모든 가격대를 순회하며 매칭
    private Collection<MatchedEvent> processMarketOrder(final TradeOrder order) {
        return elements.keySet().stream()
                .map(price -> match(price, order))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toList();
    }

    // 지정가 주문 처리: 해당 가격대에서만 매칭
    private Collection<MatchedEvent> processLimitOrder(final TradeOrder order) {
        final Price now = new Price(order.getPrice());
        return match(now, order);
    }

    // 입력 받은 가격대에서 주문 매칭
    private List<MatchedEvent> match(final Price price, final TradeOrder order) {
        final OrderStorage storage = elements.get(price);
        if (storage == null || storage.isEmpty()) {
            return List.of();
        }
        return storage.match(order);
    }

    // 남은 수량의 주문을 호가창에 추가
    public void add(final TradeOrder order) {
        if (order.hasRemainingQuantity() && isCorrectOrderType(order)) {
            Price price = new Price(order.getPrice());
            elements.computeIfAbsent(price, p -> new OrderStorage());
            elements.get(price).add(order);
        }
    }

    /**
     * 입력된 주문이 현재 시장 가격보다 불리한지 판단.
     * (매도: 현재 최저가보다 높은 경우, 매수: 현재 최고가보다 낮은 경우)
     */
    public boolean isOrderWorseThanMarket(final TradeOrder order) {
        if (!isCorrectOrderType(order) || elements.isEmpty()) {
            return false;
        }
        return isWorseThanTop(order);
    }

    // 해당 호가창의 타입(매수/매도)과 주문의 타입이 일치하는지 확인.
    protected abstract boolean isCorrectOrderType(TradeOrder order);

    /**
     * 현재 호가창의 최상단 가격 대비 입력 주문이 불리한 가격인지 판단.
     * 매도일 경우 더 높은 가격, 매수일 경우 더 낮은 가격이면 true.
     */
    protected abstract boolean isWorseThanTop(TradeOrder order);
}
