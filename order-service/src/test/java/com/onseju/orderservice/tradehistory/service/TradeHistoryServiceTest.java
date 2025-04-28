package com.onseju.orderservice.tradehistory.service;

import com.onseju.orderservice.events.dto.MatchedEvent;
import com.onseju.orderservice.fake.FakeOrderRepository;
import com.onseju.orderservice.fake.FakeTradeHistoryRepository;
import com.onseju.orderservice.tradehistory.domain.TradeHistory;
import com.onseju.orderservice.tradehistory.mapper.TradeHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TradeHistoryServiceTest {

	private FakeTradeHistoryRepository tradeHistoryRepository;
	private TradeHistoryService tradeHistoryService;
	private TradeHistoryMapper tradeHistoryMapper;
	private FakeOrderRepository orderRepository;

	@BeforeEach
	void setUp() {
		tradeHistoryRepository = new FakeTradeHistoryRepository();
		tradeHistoryMapper = new TradeHistoryMapper();
		orderRepository = new FakeOrderRepository();
		tradeHistoryService = new TradeHistoryService(tradeHistoryRepository, orderRepository, tradeHistoryMapper);
	}

	@Test
	@DisplayName("체결 내역을 저장한다.")
	void save() {
		// given
		MatchedEvent matchedEvent = new MatchedEvent(
				UUID.randomUUID(),
				"005930",
				1L,
				1L,
				2L,
				2L,
				new BigDecimal(10),
				new BigDecimal(1000),
				Instant.now().getEpochSecond()
		);

		// when
		TradeHistory saved = tradeHistoryService.save(matchedEvent);

		// then
		assertThat(saved).isNotNull();
		assertThat(saved.getCompanyCode()).isEqualTo(matchedEvent.companyCode());
		assertThat(saved.getQuantity()).isEqualTo(matchedEvent.quantity());
		assertThat(saved.getBuyOrderId()).isEqualTo(matchedEvent.buyOrderId());
		assertThat(saved.getSellOrderId()).isEqualTo(matchedEvent.sellOrderId());
		assertThat(saved.getTradeTime()).isEqualTo(matchedEvent.tradeAt());
	}
}
