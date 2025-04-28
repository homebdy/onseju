package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.CompanyCode;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.orderbook.AbstractOrderBook;
import com.onseju.matchingservice.engine.orderbook.BuyOrderBook;
import com.onseju.matchingservice.engine.orderbook.SellOrderBook;
import com.onseju.matchingservice.events.MatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MatchingEngine
 * 1. CompanyCode를 기준으로 주문장(OrderBook) 관리
 * 2. 주문 타입(SELL/BUY) 확인
 * 3. 적절한 OrderBook 에서 매칭 시도
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    // 각 회사별 매도 주문을 저장하는 OrderBook
    private final ConcurrentHashMap<CompanyCode, AbstractOrderBook> sellOrderBook = new ConcurrentHashMap<>();

    // 각 회사별 매수 주문을 저장하는 OrderBook
    private final ConcurrentHashMap<CompanyCode, AbstractOrderBook> buyOrderBook = new ConcurrentHashMap<>();

    // 매칭 동시성 문제를 해결하기 위한 Lock
    ReentrantLock lock = new ReentrantLock(true);

    /**
     * 주문을 처리한다.
     * 처리 순서:
     * 1. 해당 회사의 주문장이 초기화되어 있지 않다면 초기화
     * 2. 주문의 타입(시장가/지정가 등)을 조정
     * 3. 주문 매칭 로직 수행
     */
    public Collection<MatchedEvent> processOrder(final TradeOrder order) {
        lock.lock();
        initializeOrderBooksIfNeeded(order.getCompanyCode());
        adjustOrderType(order);
        Collection<MatchedEvent> results = processOrderMatching(order);
        lock.unlock();
        return results;
    }

    // 회사별 매도/매수 주문장이 존재하지 않으면 새로 생성
    private void initializeOrderBooksIfNeeded(final CompanyCode companyCode) {
        sellOrderBook.computeIfAbsent(companyCode, key -> new SellOrderBook());
        buyOrderBook.computeIfAbsent(companyCode, key -> new BuyOrderBook());
    }

    // 주문이 시장 상황보다 불리한 조건일 경우 시장가로 타입 조정
    private void adjustOrderType(final TradeOrder order) {
        if (order.isSellType()) {
            adjustSellOrderType(order);
        } else {
            adjustBuyOrderType(order);
        }
    }

    // 매도 주문의 타입을 조정
    private void adjustSellOrderType(final TradeOrder order) {
        AbstractOrderBook orderBook = sellOrderBook.get(order.getCompanyCode());
        if (orderBook.isOrderWorseThanMarket(order)) {
            order.changeTypeToMarket();
        }
    }

    // 매수 주문의 타입을 조정
    private void adjustBuyOrderType(final TradeOrder order) {
        AbstractOrderBook orderBook = buyOrderBook.get(order.getCompanyCode());
        if (orderBook.isOrderWorseThanMarket(order)) {
            order.changeTypeToMarket();
        }
    }

    // 주문을 실제로 매칭 처리하고, 남은 주문은 주문장에 추가
    private Collection<MatchedEvent> processOrderMatching(final TradeOrder order) {
        AbstractOrderBook matchingOrderBook = findMatchingOrderBook(order);
        Collection<MatchedEvent> matchedEvents = matchingOrderBook.matchOrder(order);

        addRemainingOrder(order);

        return matchedEvents;
    }

    // 매칭 대상이 되는 반대편 주문장을 반환
    private AbstractOrderBook findMatchingOrderBook(final TradeOrder order) {
        if (order.isSellType()) {
            return buyOrderBook.get(order.getCompanyCode());
        }
        return sellOrderBook.get(order.getCompanyCode());
    }

    // 매칭되지 않고 남은 주문을 현재 주문 타입에 해당하는 주문장에 추가
    private void addRemainingOrder(final TradeOrder order) {
        if (order.isSellType()) {
            AbstractOrderBook orderBook = sellOrderBook.get(order.getCompanyCode());
            orderBook.add(order);
            return;
        }
        AbstractOrderBook orderBook = buyOrderBook.get(order.getCompanyCode());
        orderBook.add(order);
    }
}
