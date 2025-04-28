package com.onseju.orderservice.order.service.validator;

import com.onseju.orderservice.order.exception.OrderPriceQuotationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PriceUnitTest {

    @Test
    @DisplayName("가격에 맞는 PriceUnit가 반환된다.")
    void getUnitByPrice() {
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(0))).isEqualTo(PriceUnit.UNIT_1);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(1000))).isEqualTo(PriceUnit.UNIT_1);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(2000))).isEqualTo(PriceUnit.UNIT_5);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(7500))).isEqualTo(PriceUnit.UNIT_10);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(25000))).isEqualTo(PriceUnit.UNIT_50);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(100000))).isEqualTo(PriceUnit.UNIT_100);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(300000))).isEqualTo(PriceUnit.UNIT_500);
        assertThat(PriceUnit.getUnitByPrice(new BigDecimal(700000))).isEqualTo(PriceUnit.UNIT_1000);
    }

    @Test
    @DisplayName("입력이 음수일 경우 예외가 발생한다.")
    void getUnitByInvalidPrice() {
        assertThatThrownBy(() -> PriceUnit.getUnitByPrice(new BigDecimal("-1000")))
                .isInstanceOf(OrderPriceQuotationException.class);
    }
}
