package com.onseju.matchingservice.events;

import java.util.List;
import java.util.UUID;

import com.onseju.matchingservice.dto.PriceLevelDto;

import lombok.Builder;

@Builder
public record OrderBookSyncedEvent(
		UUID id,
		String companyCode,
		List<PriceLevelDto> sellLevels,
		List<PriceLevelDto> buyLevels
) {
}
