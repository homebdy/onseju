package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.events.MatchedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderStorage {

	private final ConcurrentSkipListSet<TradeOrder> elements = new ConcurrentSkipListSet<>(
			Comparator.comparing(TradeOrder::getTimestamp)
					.thenComparing(TradeOrder::getTotalQuantity, Comparator.reverseOrder())
					.thenComparing(TradeOrder::getId)
	);

	// Set 내에 존재하는 주문과 입력된 주문을 매칭힌다.
	public List<MatchedEvent> match(final TradeOrder incomingOrder) {
		Iterator<TradeOrder> iterator = elements.iterator();
		List<MatchedEvent> results = new ArrayList<>();
		while (iterator.hasNext() && incomingOrder.hasRemainingQuantity()) {
			final TradeOrder foundedOrder = iterator.next();
			if (foundedOrder.isSameAccount(incomingOrder.getAccountId())) {
				continue;
			}

			BigDecimal matchedQuantity = incomingOrder.calculateMatchQuantity(foundedOrder);
			updateRemainingQuantity(incomingOrder, foundedOrder, matchedQuantity);
			results.add(createResponse(incomingOrder, foundedOrder, matchedQuantity));
			if (!foundedOrder.hasRemainingQuantity()) {
				iterator.remove();
			}
		}
		return results;
	}

	// 체결 완료 후 남은 수량 감소 및 완료 여부 확인
	private void updateRemainingQuantity(
			final TradeOrder incomingOrder,
			final TradeOrder foundedOrder,
			final BigDecimal matchedQuantity
	) {
		incomingOrder.decreaseRemainingQuantity(matchedQuantity);
		foundedOrder.decreaseRemainingQuantity(matchedQuantity);
	}

	// 매칭 완료 후 응답 생성
	private MatchedEvent createResponse(
			final TradeOrder incomingOrder,
			final TradeOrder foundOrder,
			final BigDecimal matchedQuantity
	) {
		final BigDecimal price = getMatchingPrice(incomingOrder, foundOrder);
		if (incomingOrder.isSellType()) {
			return new MatchedEvent(
					UUID.randomUUID(),
					incomingOrder.getCompanyCode(),
					foundOrder.getId(),
					foundOrder.getAccountId(),
					incomingOrder.getId(),
					incomingOrder.getAccountId(),
					matchedQuantity,
					price,
					Instant.now().toEpochMilli()
			);
		}
		return new MatchedEvent(
				UUID.randomUUID(),
				incomingOrder.getCompanyCode(),
				incomingOrder.getId(),
				incomingOrder.getAccountId(),
				foundOrder.getId(),
				foundOrder.getAccountId(),
				matchedQuantity,
				price,
				Instant.now().toEpochMilli()
		);
	}

	// 매칭 가격을 계산한다.
	private BigDecimal getMatchingPrice(final TradeOrder incomingOrder, final TradeOrder foundOrder) {
		if (incomingOrder.isMarketOrder()) {
			return foundOrder.getPrice();
		}
		return incomingOrder.getPrice();
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public void add(TradeOrder order) {
		elements.add(order);
	}

	/**
	 * 주문 개수 반환
	 */
	public Integer getOrderCount() {
		return elements.size();
	}

	/**
	 * 총 주문 수량 계산
	 */
	public BigDecimal getTotalQuantity() {
		return elements.stream()
				.map(order -> order.getRemainingQuantity().get())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
