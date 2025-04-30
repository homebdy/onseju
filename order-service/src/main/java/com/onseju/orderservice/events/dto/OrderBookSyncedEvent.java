package com.onseju.orderservice.events.dto;

import com.onseju.orderservice.order.dto.PriceLevelDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderBookSyncedEvent(
        UUID id,
        String companyCode,
        List<PriceLevelDto> sellLevels,
        List<PriceLevelDto> buyLevels
) {
}
