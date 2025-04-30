package com.onseju.orderservice.order.service.validator;

import com.onseju.orderservice.company.domain.Company;
import com.onseju.orderservice.order.exception.OrderPriceQuotationException;
import com.onseju.orderservice.order.exception.PriceOutOfRangeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
public class OrderValidator {

    // 주문 유효성 검사
    public static void validateOrder(final BigDecimal price, final Company company) {
        validatePriceUnit(price);
        validateClosingPriceRange(price, company);
    }

    // 지정가 주문 가격 견젹 유효성 검증
    private static void validatePriceUnit(final BigDecimal price) {
        PriceUnit priceUnit = PriceUnit.getUnitByPrice(price);
        if (price.remainder(priceUnit.getUnit()).compareTo(BigDecimal.ZERO) != 0) {
            throw new OrderPriceQuotationException("주문 가격이 호가 단위에 맞지 않습니다.");
        }
    }

    // 종가 가격 기준 15% 이내의 금액인지 검증
    private static void validateClosingPriceRange(final BigDecimal price, final Company company) {
        if (!company.isWithinClosingPriceRange(price)) {
            throw new PriceOutOfRangeException();
        }
    }
}
