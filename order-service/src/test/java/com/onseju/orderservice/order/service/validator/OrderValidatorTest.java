package com.onseju.orderservice.order.service.validator;

import com.onseju.orderservice.company.domain.Company;
import com.onseju.orderservice.order.exception.OrderPriceQuotationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderValidatorTest {

    private final Company company = Company.builder()
            .isuSrtCd("005930")
            .closingPrice(new BigDecimal("1000"))
            .build();

    @Test
    @DisplayName("유효한 가격을 입력할 경우 정상 처리된다.")
    void isValidPrice() {
        assertThatNoException().isThrownBy(() -> OrderValidator.validateOrder(new BigDecimal(1_001), company));
    }

    @Test
    @DisplayName("잘못된 가격이 예외를 발생시키는지 확인")
    void isValidInvalidPrice() {
        BigDecimal OrderValidator1 = new BigDecimal("0.5");
        BigDecimal OrderValidator5 = new BigDecimal(2_001);
        BigDecimal OrderValidator10 = new BigDecimal(5_005);
        BigDecimal OrderValidator50 = new BigDecimal(20_010);
        BigDecimal OrderValidator100 = new BigDecimal(50_050);
        BigDecimal OrderValidator500 = new BigDecimal(200_100);
        BigDecimal OrderValidator1000 = new BigDecimal(500_500);

        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator1, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator5, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator10, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator50, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator100, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator500, company)).isInstanceOf(OrderPriceQuotationException.class);
        assertThatThrownBy(() -> OrderValidator.validateOrder(OrderValidator1000, company)).isInstanceOf(OrderPriceQuotationException.class);
    }
}
