package com.onseju.orderservice.tradehistory.controller.response;

import com.onseju.orderservice.tradehistory.dto.TotalTradeAmountDto;
import com.onseju.orderservice.tradehistory.dto.TradeAvgPriceDto;
import com.onseju.orderservice.tradehistory.dto.TradeCountDto;

import java.util.List;

public record RankingResponse(
        List<TotalTradeAmountDto> totalTradeAmounts,
        List<TradeAvgPriceDto> tradeAvgPrices,
        List<TradeCountDto> tradeCounts
) {
}
