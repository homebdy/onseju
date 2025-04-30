package com.onseju.orderservice.chart.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ChartResponseDto(
        List<CandleDto> candles,
        String timeCode
) {
}
