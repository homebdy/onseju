package com.onseju.userservice.account.service.dto;

import com.onseju.userservice.account.domain.Type;

import java.math.BigDecimal;

public record MatchedAccountUpdateDto(
        Long memberId,
        Type type,
        BigDecimal price,
        BigDecimal quantity
) {
}
