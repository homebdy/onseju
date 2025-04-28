package com.onseju.orderservice.fake;

import com.onseju.orderservice.order.domain.Order;
import com.onseju.orderservice.tradehistory.domain.TradeHistory;
import com.onseju.orderservice.tradehistory.service.repository.TradeHistoryRepository;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FakeTradeHistoryRepository implements TradeHistoryRepository {

	private final List<TradeHistory> elements = new ArrayList<>();

	@Override
	public TradeHistory save(TradeHistory tradeHistory) {
		if (hasElement(tradeHistory)) {
			elements.remove(elements.indexOf(tradeHistory));
			elements.add(tradeHistory);
			return tradeHistory;
		}
		TradeHistory saved = TradeHistory.builder()
				.id((long) (elements.size() + 1))
				.companyCode(tradeHistory.getCompanyCode())
				.sellOrderId(tradeHistory.getSellOrderId())
				.buyOrderId(tradeHistory.getBuyOrderId())
				.price(tradeHistory.getPrice())
				.quantity(tradeHistory.getQuantity())
				.tradeTime(tradeHistory.getTradeTime())
				.build();
		elements.add(tradeHistory);
		return saved;
	}

	private boolean hasElement(TradeHistory tradeHistory) {
		return elements.stream()
				.anyMatch(h -> h.getId().equals(tradeHistory.getId()));
	}

	public TradeHistory getById(Long id) {
		return elements.stream()
				.filter(e -> Objects.equals(e.getId(), id))
				.findAny()
				.orElseThrow();
	}

	@Override
	public List<String> findDistinctCompanyCodes() {
		return List.of();
	}

	@Override
	public List<TradeHistory> findRecentTradesByCompanyCode(String companyCode, Integer limit) {
		return List.of();
	}

	@Override
	public List<Object[]> findTotalTradeAmountByCompany(Pageable pageable) {
		return List.of();
	}

	@Override
	public List<Object[]> findTradeAvgPriceByCompany(Pageable pageable) {
		return List.of();
	}

	@Override
	public List<Object[]> findTradeCountByCompany(Pageable pageable) {
		return List.of();
	}

	@Override
	public List<TradeHistory> findByOrderId(Order order) {
		return List.of();
	}
}