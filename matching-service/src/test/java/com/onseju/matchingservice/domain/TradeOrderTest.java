package com.onseju.matchingservice.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TradeOrderTest {

    private TradeOrder buyOrder;
    private TradeOrder sellOrder;

    @BeforeEach
    void setUp() {
        buyOrder = TradeOrder.builder()
                .id(1L)
                .companyCode("ABC")
                .type(Type.LIMIT_BUY)
                .status(OrderStatus.ACTIVE)
                .totalQuantity(BigDecimal.valueOf(100))
                .remainingQuantity(new AtomicReference<>(BigDecimal.valueOf(100)))
                .price(BigDecimal.valueOf(5000))
                .accountId(10L)
                .build();

        sellOrder = TradeOrder.builder()
                .id(2L)
                .companyCode("ABC")
                .type(Type.LIMIT_SELL)
                .status(OrderStatus.ACTIVE)
                .totalQuantity(BigDecimal.valueOf(50))
                .remainingQuantity(new AtomicReference<>(BigDecimal.valueOf(50)))
                .price(BigDecimal.valueOf(5000))
                .accountId(20L)
                .build();
    }

    @Test
    @DisplayName("id값이 같을 경우 true를 반환다.")
    void returnTrueWhenSameId() {
        // given
        TradeOrder order1 = TradeOrder.builder().id(1L).build();
        TradeOrder order2 = TradeOrder.builder().id(1L).build();

        // when
        boolean result = order1.equals(order2);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("id값이 다르거나 null일 경우 false를 반환다.")
    void returnFalseWhenDifferentId() {
        // given
        TradeOrder order1 = TradeOrder.builder().id(1L).build();
        TradeOrder order2 = TradeOrder.builder().id(2L).build();
        TradeOrder order3 = null;

        // when
        boolean result = order1.equals(order2);
        boolean result2 = order1.equals(order3);

        // then
        assertThat(result).isFalse();
        assertThat(result2).isFalse();
    }

    @Test
    @DisplayName("같은 계정으로부터의 주문일 경우 true를 반환한다.")
    void returnTrueWhenSameAccount() {
        // given
        TradeOrder order1 = TradeOrder.builder().id(1L).accountId(1L).build();
        TradeOrder order2 = TradeOrder.builder().id(2L).accountId(1L).build();

        // when
        boolean result = order1.isSameAccount(order2.getAccountId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 계정으로부터의 주문일 경우 false를 반환한다.")
    void returnFalseWhenDifferentAccount() {
        // given
        TradeOrder order1 = TradeOrder.builder().id(1L).accountId(1L).build();
        TradeOrder order2 = TradeOrder.builder().id(2L).accountId(2L).build();

        // when
        boolean result = order1.isSameAccount(order2.getAccountId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("주문량 감소 테스트")
    void decreaseRemainingQuantity() {
        // given
        BigDecimal quantity = BigDecimal.valueOf(30);

        // when
        buyOrder.decreaseRemainingQuantity(quantity);

        // then
        assertThat(buyOrder.getRemainingQuantity().get()).isEqualByComparingTo(BigDecimal.valueOf(70));
    }

    @Test
    @DisplayName("남은 양이 0이하가 되지 않는다")
    void remainZeroWhendecreaseRemainingQuantity() {
        // given
        BigDecimal quantity = BigDecimal.valueOf(100);

        // when
        sellOrder.decreaseRemainingQuantity(quantity);

        // then
        assertThat(sellOrder.getRemainingQuantity().get()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("매도 주문일 경우 true, 매수 주문일 경우 false를 반환한다.")
    void returnTrueWhenIsSell() {
        assertThat(sellOrder.isSellType()).isTrue();
        assertThat(buyOrder.isSellType()).isFalse();
    }

    @Test
    @DisplayName("매칭량 계산시, 입력된 수량과 가지고 있는 수량 중 더 적은 수량을 반환한다.")
    void calculateMatchQuantity() {
        // when
        BigDecimal matchQuantity = buyOrder.calculateMatchQuantity(sellOrder);

        // then
        assertThat(matchQuantity).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("남은 양이 0이면 OrderStatus를 Complete로 변경한다.")
    void checkAndChangeOrderStatus() {
        // when
        sellOrder.decreaseRemainingQuantity(BigDecimal.valueOf(50));

        // then
        assertThat(sellOrder.getStatus()).isEqualTo(OrderStatus.COMPLETE);
    }

    @Test
    @DisplayName("남은 수량이 있을 경우 true, 없을 경우 false를 반환한다.")
    void hasRemainingQuantity() {
        assertThat(buyOrder.hasRemainingQuantity()).isTrue();

        buyOrder.decreaseRemainingQuantity(BigDecimal.valueOf(100));
        assertThat(buyOrder.hasRemainingQuantity()).isFalse();
    }

    @Test
    @DisplayName("시장가 주문일 경우 true, 아닐 경우 false를 반환한다.")
    void isMarketOrder() {
        assertThat(buyOrder.isMarketOrder()).isFalse();

        buyOrder.changeTypeToMarket();
        assertThat(buyOrder.isMarketOrder()).isTrue();
    }

    @Test
    @DisplayName("지정가 주문을 시장가로 변경한다.")
    void changeTypeToMarket() {
        // when
        buyOrder.changeTypeToMarket();

        // then
        assertThat(buyOrder.getType()).isEqualTo(Type.MARKET_BUY);
        assertThat(buyOrder.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}