package com.onseju.userservice.order;


import com.onseju.userservice.account.domain.Type;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatedOrderDto(
        String companyCode,
        Type type,
        BigDecimal totalQuantity,
        BigDecimal price,
        Long timestamp,
        String username
) {

}
