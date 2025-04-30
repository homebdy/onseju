package com.onseju.matchingservice.service;

import com.onseju.matchingservice.domain.CompanyCode;
import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.engine.orderbook.SellOrderBook;
import com.onseju.matchingservice.events.dto.MatchedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

public class SellOrderBookTest {

    private final SellOrderBook sellOrderBook = new SellOrderBook();

    @Test
    @DisplayName("지정가 매도 주문 추가")
    void receiveLimitSellOrder() {
        // given
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 1L);

        // when, then
        assertThatNoException()
                .isThrownBy(() -> sellOrderBook.add(sellOrder));
    }

    @Test
    @DisplayName("지정가 매수 주문 시, 일치하는 가격의 매도 주문과 체결된다.")
    void matchBuyOrderWithSamePriceSellOrder() {
        // given
        TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
        TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("5"), 1L);
        TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);

        // when
        sellOrderBook.add(sellOrder1);
        sellOrderBook.add(sellOrder2);
        Collection<MatchedEvent> responses = sellOrderBook.matchOrder(buyOrder);

        // then
        assertThat(responses).hasSize(1);
        responses.forEach(result -> {
            assertThat(result.sellOrderId()).isEqualTo(sellOrder1.getId());
            assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
            assertThat(result.price()).isEqualTo(buyOrder.getPrice());
            assertThat(result.quantity()).isEqualTo(buyOrder.getTotalQuantity());
        });
    }

    @Test
    @DisplayName("주문에 대한 체결이 완료된 경우 상태를 COMPLETE로 변경")
    void changeOrderStatusToCompleteWhenTradeIsExecuted() {
        // given
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
        TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);

        // when
        sellOrderBook.add(sellOrder);
        sellOrderBook.matchOrder(buyOrder);

        // then
        assertThat(sellOrder.getStatus()).isEqualTo(OrderStatus.COMPLETE);
        assertThat(buyOrder.getStatus()).isEqualTo(OrderStatus.COMPLETE);
    }

    @Test
    @DisplayName("지정가 매수 주문 시, 일치하는 가격의 매도 주문과 부분 체결될 수 있다.")
    void matchPartialSellOrderWithSamePriceSellOrder() {
        // given
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
        TradeOrder buyOrder = createOrder(2L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("10"), 2L);

        // when
        sellOrderBook.add(sellOrder);
        Collection<MatchedEvent> responses = sellOrderBook.matchOrder(buyOrder);

        // then
        assertThat(responses).hasSize(1);
        assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
        responses.forEach(result -> {
            assertThat(result.sellOrderId()).isEqualTo(sellOrder.getId());
            assertThat(result.sellMemberId()).isEqualTo(sellOrder.getMemberId());
            assertThat(result.buyOrderId()).isEqualTo(buyOrder.getId());
            assertThat(result.buyMemberId()).isEqualTo(buyOrder.getMemberId());
            assertThat(result.price()).isEqualTo(buyOrder.getPrice());
            assertThat(result.quantity()).isEqualTo(
                    buyOrder.getTotalQuantity().subtract(buyOrder.getRemainingQuantity().get()));
        });
    }

    @Test
    @DisplayName("매도 주문 불균형 상황 테스트")
    void sellOrderImbalance() {
        // given
        for (int i = 0; i < 5; i++) {
            TradeOrder sellOrder = createOrder((long) i, Type.LIMIT_SELL, new BigDecimal("50000"),
                    new BigDecimal("5"), 1L);
            sellOrderBook.add(sellOrder);
        }

        // when
        TradeOrder buyOrder = createOrder(5L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L);
        Collection<MatchedEvent> response = sellOrderBook.matchOrder(buyOrder);

        // then
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("시장가 매수 주문 처리")
    void receiveMarketBuyOrder() {
        // given
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
        TradeOrder marketBuyOrder = createOrder(2L, Type.MARKET_BUY, BigDecimal.ZERO, new BigDecimal("5"), 2L);

        // when
        sellOrderBook.add(sellOrder);
        List<MatchedEvent> responses = new ArrayList<>(sellOrderBook.matchOrder(marketBuyOrder));

        // then
        assertThat(responses).hasSize(1);
        responses.forEach(res -> {
            assertThat(res.buyOrderId()).isEqualTo(marketBuyOrder.getId());
            assertThat(res.sellOrderId()).isEqualTo(sellOrder.getId());
            assertThat(res.quantity()).isEqualTo(marketBuyOrder.getTotalQuantity());
            assertThat(res.price()).isEqualTo(sellOrder.getPrice());
        });
    }

    @Test
    @DisplayName("시장가 매수 주문 시, 낮은 가격의 매도 주문 부터 높은 가격의 주문 순으로 여러 가격대의 주문과 체결될 수 있다.")
    void matchBuyMarketOrderWithLowestPrice() {
        // given
        // 지정가 매도 주문 2개 추가 (서로 다른 가격)
        TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("5"), 1L);
        TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L);
        TradeOrder marketBuyOrder = createOrder(3L, Type.MARKET_BUY, BigDecimal.ZERO, new BigDecimal("10"), 2L);

        // When
        sellOrderBook.add(sellOrder1);
        sellOrderBook.add(sellOrder2);
        List<MatchedEvent> responses = new ArrayList<>(sellOrderBook.matchOrder(marketBuyOrder));

        // Then
        // 호가창 확인
        assertThat(responses).hasSize(2);

        MatchedEvent sellOrderEvent1 = responses.get(0);
        assertThat(sellOrderEvent1.sellOrderId()).isEqualTo(sellOrder1.getId());
        assertThat(sellOrderEvent1.buyOrderId()).isEqualTo(marketBuyOrder.getId());
        assertThat(sellOrderEvent1.price()).isEqualTo(sellOrder1.getPrice());

        MatchedEvent sellOrderEvent2 = responses.get(1);
        assertThat(sellOrderEvent2.sellOrderId()).isEqualTo(sellOrder2.getId());
        assertThat(sellOrderEvent2.buyOrderId()).isEqualTo(marketBuyOrder.getId());
        assertThat(sellOrderEvent2.price()).isEqualTo(sellOrder2.getPrice());
    }


    private TradeOrder createOrder(Long id, Type type, BigDecimal price, BigDecimal quantity, Long memberId) {
        return TradeOrder.builder()
                .id(id)
                .type(type)
                .price(price)
                .memberId(memberId)
                .companyCode(new CompanyCode("005930"))
                .status(OrderStatus.ACTIVE)
                .totalQuantity(quantity)
                .remainingQuantity(new AtomicReference<>(quantity))
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    @Test
    @DisplayName("매도 주문시, 같은 가격일 경우 먼저 주문이 들어온 주문부터 처리한다.")
    void sellOrderTimePriorityMatching() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L,
                createdAt.minusSeconds(1));
        TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L,
                createdAt);
        TradeOrder buyOrder = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L,
                createdAt);

        sellOrderBook.add(sellOrder1);
        sellOrderBook.add(sellOrder2);
        sellOrderBook.matchOrder(buyOrder);

        assertThat(sellOrder1.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
        assertThat(sellOrder2.getRemainingQuantity().get()).isEqualTo(new BigDecimal("5"));
        assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("매도 주문시, 모든 조건이 일치할 경우 수량이 많은 주문부터 체결한다.")
    void sellOrderQuantityPriorityMatching() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        TradeOrder sellOrder1 = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("10"), 1L,
                createdAt);
        TradeOrder sellOrder2 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 1L,
                createdAt);
        TradeOrder buyOrder = createOrder(3L, Type.MARKET_BUY, new BigDecimal("50000"), new BigDecimal("5"), 2L,
                createdAt);

        sellOrderBook.add(sellOrder2);
        sellOrderBook.add(sellOrder1);
        sellOrderBook.matchOrder(buyOrder);

        assertThat(sellOrder1.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
        assertThat(sellOrder2.getRemainingQuantity().get()).isEqualTo(new BigDecimal(5));
        assertThat(buyOrder.getRemainingQuantity().get()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("자신의 주문과는 매칭되지 않는다.")
    void notMatchWithSameAccountId() {
        // given
        Long accountId = 1L;
        TradeOrder sellOrder
                = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), accountId);
        TradeOrder buyOrder
                = createOrder(3L, Type.LIMIT_BUY, new BigDecimal("50000"), new BigDecimal("5"), accountId);

        // when
        sellOrderBook.add(sellOrder);
        sellOrderBook.matchOrder(buyOrder);

        // then
        assertThat(sellOrder.getStatus()).isNotEqualTo(OrderStatus.COMPLETE);
        assertThat(buyOrder.getStatus()).isNotEqualTo(OrderStatus.COMPLETE);
    }

    @Test
    @DisplayName("매도 주문시, 시장가보다 가격이 낮은 경우 true를 반환한다.")
    void isSellOrderBelowMarketPrice() {
        // given: 시장가 5만원 형성
        TradeOrder sellOrder1 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
        TradeOrder sellOrder2 = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
        sellOrderBook.add(sellOrder1);
        sellOrderBook.add(sellOrder2);

        // when
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("49000"), new BigDecimal("10"), 1L);
        boolean result = sellOrderBook.isOrderWorseThanMarket(sellOrder);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("매도 주문시, 시장가보다 가격이 높은 경우 false를 반환한다.")
    void isSellOrderHigherThanMarketPrice() {
        // given: 시장가 5만원 형성
        TradeOrder sellOrder1 = createOrder(2L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
        TradeOrder sellOrder2 = createOrder(3L, Type.LIMIT_SELL, new BigDecimal("50000"), new BigDecimal("5"), 2L);
        sellOrderBook.add(sellOrder1);
        sellOrderBook.add(sellOrder2);

        // when
        TradeOrder sellOrder = createOrder(1L, Type.LIMIT_SELL, new BigDecimal("51000"), new BigDecimal("10"), 1L);
        boolean result = sellOrderBook.isOrderWorseThanMarket(sellOrder);

        // then
        assertThat(result).isFalse();
    }

    private TradeOrder createOrder(Long id, Type type, BigDecimal price, BigDecimal quantity, Long memberId,
                                   LocalDateTime createdDateTime) {
        return TradeOrder.builder()
                .id(id)
                .type(type)
                .price(price)
                .memberId(memberId)
                .companyCode(new CompanyCode("005930"))
                .status(OrderStatus.ACTIVE)
                .totalQuantity(quantity)
                .remainingQuantity(new AtomicReference<>(quantity))
                .timestamp(createdDateTime.toEpochSecond(ZoneOffset.UTC))
                .build();
    }
}
