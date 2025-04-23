package com.onseju.matchingservice.engine;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

import com.onseju.matchingservice.domain.Price;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.dto.PriceLevelDto;
import com.onseju.matchingservice.events.MatchedEvent;
import com.onseju.matchingservice.events.OrderBookSyncedEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 종복별로 주문을 관리한다.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class CompanyOrderBook {

	// 매도 주문: 낮은 가격 우선
	private final ConcurrentSkipListMap<Price, OrderStorage> sellOrders = new ConcurrentSkipListMap<>(
			Comparator.comparing(Price::getValue)
	);

	// 매수 주문: 높은 가격 우선
	private final ConcurrentSkipListMap<Price, OrderStorage> buyOrders = new ConcurrentSkipListMap<>(
			Comparator.comparing(Price::getValue).reversed()
	);

	private final ReentrantLock matchlock = new ReentrantLock();

	/**
	 * 주문을 시장가, 지정가로 나누어 처리한다.
	 */
	public List<MatchedEvent> received(final TradeOrder order) {
		if (order.isMarketOrder()) {
			return processMarketOrder(order);
		}
		return processLimitOrder(order);
	}

	/**
	 * 시장가 주문: 주문을 매칭한 후, 남은 수량에 대한 매칭을 더 이상 진행하지 않는다.
	 */
	private List<MatchedEvent> processMarketOrder(final TradeOrder order) {
		ConcurrentSkipListMap<Price, OrderStorage> orders = getCounterOrders(order.getType());

		return orders.keySet().stream()
				.map(price -> match(price, order))
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.toList();
	}

	private ConcurrentSkipListMap<Price, OrderStorage> getCounterOrders(final Type type) {
		if (type.isSell()) {
			return buyOrders;
		}
		return sellOrders;
	}

	/**
	 * 지정가 주문: 주문을 매칭한 후, 남은 수량을 OrderStorage에 추가한다.
	 */
	private List<MatchedEvent> processLimitOrder(final TradeOrder order) {
		matchlock.lock();
		final Price now = new Price(order.getPrice());
		List<MatchedEvent> result = match(now, order);
		if (order.hasRemainingQuantity()) {
			addRemainingTradeOrder(order);
		}
		matchlock.unlock();
		return result;
	}

	/**
	 * 입력한 가격대의 주문과 매칭한다.
	 */
	private List<MatchedEvent> match(final Price price, final TradeOrder order) {
		final OrderStorage orderStorage = getCounterOrderStorage(price, order.getType());
		if (orderStorage == null || orderStorage.isEmpty()) {
			return List.of();
		}
		return orderStorage.match(order);
	}

	/**
	 * 같은 타입(매도, 매수)의 주문을 저장하는 OrderStorage 조회한다. 존재하지 않을 경우 새로 생성하여 반환한다.
	 */
	private OrderStorage getOrCreateSameTypeOrderStorage(final Price price, final Type type) {
		if (type.isSell()) {
			return sellOrders.computeIfAbsent(price, p -> new OrderStorage());
		}
		return buyOrders.computeIfAbsent(price, p -> new OrderStorage());
	}

	/**
	 * 다른 타입(매도, 매수)의 주문을 저장하는 OrderStorage 조회한다.
	 */
	private OrderStorage getCounterOrderStorage(final Price price, final Type type) {
		if (type.isSell()) {
			return buyOrders.get(price);
		}
		return sellOrders.get(price);
	}

	/**
	 * 남은 주문을 OrderStorage에 저장한다.
	 */
	private void addRemainingTradeOrder(final TradeOrder order) {
		Price price = new Price(order.getPrice());
		OrderStorage orderStorage = getOrCreateSameTypeOrderStorage(price, order.getType());
		orderStorage.add(order);
	}

	public boolean isSellOrderBelowMarketPrice(TradeOrder order) {
		if (!order.isSellType() || sellOrders.isEmpty()) {
			return false;
		}
		Price lowestSellOrder = sellOrders.firstKey();
		return lowestSellOrder.isHigherThan(order.getPrice());
	}

	public boolean isBuyOrderAboveMarketPrice(TradeOrder order) {
		if (order.isSellType() || buyOrders.isEmpty()) {
			return false;
		}
		Price highestBuyOrder = buyOrders.firstKey();
		return !highestBuyOrder.isHigherThan(order.getPrice());
	}

	/**
	 * 호가창 데이터 생성
	 */
	public OrderBookSyncedEvent getOrderBook(final String companyCode) {
		// 상위 10개 매도/매수 호가 추출
		final List<PriceLevelDto> sellLevels = getTopPriceLevels(sellOrders);
		final List<PriceLevelDto> buyLevels = getTopPriceLevels(buyOrders);
		return new OrderBookSyncedEvent(UUID.randomUUID(), companyCode, sellLevels, buyLevels);
	}

	/**
	 * 주문장에서 상위 N개 가격대의 호가 정보 추출
	 */
	private List<PriceLevelDto> getTopPriceLevels(final ConcurrentSkipListMap<Price, OrderStorage> orders) {
		return orders.entrySet().stream()
				.limit(10)
				.map(entry -> {
					final OrderStorage storage = entry.getValue();
					final BigDecimal price = entry.getKey().getValue();
					final BigDecimal quantity = storage.getTotalQuantity();
					final Integer orderCount = storage.getOrderCount();

					return PriceLevelDto.builder()
							.price(price)
							.quantity(quantity)
							.orderCount(orderCount)
							.build();
				}).toList();
	}
}
