package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.CompanyCode;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.orderbook.AbstractOrderBook;
import com.onseju.matchingservice.engine.orderbook.BuyOrderBook;
import com.onseju.matchingservice.engine.orderbook.SellOrderBook;
import com.onseju.matchingservice.events.MatchedEvent;
import com.onseju.matchingservice.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    // 각 회사별 매도 주문을 저장하는 OrderBook
    private final ConcurrentHashMap<CompanyCode, AbstractOrderBook> sellOrderBook = new ConcurrentHashMap<>();

    // 각 회사별 매수 주문을 저장하는 OrderBook
    private final ConcurrentHashMap<CompanyCode, AbstractOrderBook> buyOrderBook = new ConcurrentHashMap<>();
    private final EventPublisher<MatchedEvent> matchedEventPublisher;

    /**
     * 주문을 처리한다.
     * 처리 순서:
     * 1. 해당 회사의 주문장이 초기화되어 있지 않다면 초기화
     * 2. 주문의 타입(시장가/지정가 등)을 조정
     * 3. 주문 매칭 로직 수행
     */
    public void processOrder(final TradeOrder order) {
        initializeOrderBooksIfNeeded(order.getCompanyCode());
        adjustOrderType(order);
        processOrderMatching(order);
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
    private void processOrderMatching(final TradeOrder order) {
        AbstractOrderBook matchingOrderBook = findMatchingOrderBook(order);
        Collection<MatchedEvent> matchedEvents = matchingOrderBook.matchOrder(order);

        addRemainingOrder(order);
        processMatchedEvents(matchedEvents);
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

    // 모든 매칭 이벤트를 순회하며 개별 이벤트 처리
    private void processMatchedEvents(final Collection<MatchedEvent> events) {
        events.forEach(event -> {
            log.info("체결 완료: sell order - {}, buy order - {}",
                    event.sellOrderId(),
                    event.buyOrderId());
            matchedEventPublisher.publishEvent(event);
        });
    }
}
