package com.onseju.matchingservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceTest {

    @Test
    @DisplayName("입력된 금액보다 큰 금액일 경우 true를 반환한다.")
    void returnTrueWhenValueHigherThanInputPrice() {
        // given
        Price price = new Price(new BigDecimal(1000));
        BigDecimal input = new BigDecimal(900);

        // when
        boolean result = price.isHigherThan(input);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("입력된 금액보다 적은 금액일 경우 false를 반환한다.")
    void returnFalseWhenValueLowerThanInputPrice() {
        // given
        Price price = new Price(new BigDecimal(1000));
        BigDecimal input = new BigDecimal(2000);

        // when
        boolean result = price.isHigherThan(input);

        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("같은 금액일 경우 true를 반환한다.")
    void equals() {
        // given
        Price price1 = new Price(new BigDecimal(1000));
        Price price2 = new Price(new BigDecimal(1000));

        // when
        boolean result = price1.equals(price2);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("입력된 금액이 다른 금액이거나 null일 경우 true를 반환한다.")
    void returnFalseWhenDifferentPrice() {
        // given
        Price price1 = new Price(new BigDecimal(1000));
        Price price2 = new Price(new BigDecimal(1100));
        Price price3 = null;

        // when
        boolean result1 = price1.equals(price2);
        boolean result2 = price1.equals(price3);

        // then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }
}