package com.onseju.matchingservice.engine;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.events.MatchedEvent;
import com.onseju.matchingservice.events.OrderBookSyncedEvent;
import com.onseju.matchingservice.events.publisher.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

	// 종목 코드를 키로 하는 주문들
	private final ConcurrentHashMap<String, CompanyOrderBook> orderBooks = new ConcurrentHashMap<>();
	private final EventPublisher<MatchedEvent> matchedEventPublisher;
	private final EventPublisher<OrderBookSyncedEvent> orderBookSyncPublisher;

	public void processOrder(final TradeOrder order) {
		log.info("매칭 엔진 도착");
		final CompanyOrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
		// checkAndChangeLimitToMarket(order);
		List<MatchedEvent> results = orderBook.received(order);
		results.forEach(i -> log.info("체결 완료: sell order - " + i.sellOrderId() + ", buyOrderId - " + i.buyOrderId()));
		results.forEach(matchedEventPublisher::publishEvent);

		// 주문 체결 후 호가창 동기화 이벤트 발행
		final String companyCode = order.getCompanyCode();
		publishOrderBookSynced(companyCode, orderBook);
	}

	// 종목별 주문장 생성, 이미 존재할 경우 반환
	private CompanyOrderBook getOrCreateOrderBook(final String companyCode) {
		return orderBooks.computeIfAbsent(
				companyCode,
				key -> new CompanyOrderBook()
		);
	}

	// 지정가 주문 시, 시장가와 비교하여 시장가보다 불리할 경우 시장가로 상태 변경
	private void checkAndChangeLimitToMarket(final TradeOrder order) {
		if (order.isSellType()) {
			updateSellOrderStatusIfBelowMarketPrice(order);
			return;
		}
		updateBuyOrderStatusIfAboveMarketPrice(order);
	}

	// 매도 주문의 가격이 시장가보다 낮은 경우 시장가로 상태 변경
	private void updateSellOrderStatusIfBelowMarketPrice(final TradeOrder order) {
		CompanyOrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
		if (orderBook.isSellOrderBelowMarketPrice(order)) {
			order.changeTypeToMarket();
		}
	}

	// 매수 주문의 가격의 시장가보다 높은 경우 시장가로 상태 변경
	private void updateBuyOrderStatusIfAboveMarketPrice(final TradeOrder order) {
		CompanyOrderBook orderBook = getOrCreateOrderBook(order.getCompanyCode());
		if (orderBook.isBuyOrderAboveMarketPrice(order)) {
			order.changeTypeToMarket();
		}
	}

	/**
	 * 호가창 동기화
	 */
	private void publishOrderBookSynced(final String companyCode, final CompanyOrderBook orderBook) {
		final OrderBookSyncedEvent event = orderBook.getOrderBook(companyCode);
		orderBookSyncPublisher.publishEvent(event);
	}
}
