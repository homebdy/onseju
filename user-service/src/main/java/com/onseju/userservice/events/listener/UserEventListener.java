package com.onseju.userservice.events.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.mapper.AccountMapper;
import com.onseju.userservice.account.service.AccountService;
import com.onseju.userservice.account.service.dto.AfterTradeAccountDto;
import com.onseju.userservice.events.MatchedEvent;
import com.onseju.userservice.events.UpdateEvent;
import com.onseju.userservice.global.config.RabbitMQConfig;
import com.onseju.userservice.holding.mapper.HoldingsMapper;
import com.onseju.userservice.holding.service.HoldingsService;
import com.onseju.userservice.holding.service.dto.AfterTradeHoldingsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스의 이벤트 리스너
 * RabbitMQ를 통해 수신된 이벤트를 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {
	private final AccountService accountService;
	private final AccountMapper accountMapper;

	private final HoldingsService holdingsService;
	private final HoldingsMapper holdingsMapper;

	/**
	 * 주문 매칭 이벤트 처리
	 */
	@RabbitListener(queues = RabbitMQConfig.USER_UPDATE_QUEUE)
	public void handleOrderMatched(final MatchedEvent event) {
		// 계좌 잔액 업데이트
		final AfterTradeAccountDto buyAccountParams =
				accountMapper.toAfterTradeAccountDto(event, event.buyAccountId(), Type.BUY);
		final AfterTradeAccountDto sellAccountParams =
				accountMapper.toAfterTradeAccountDto(event, event.sellAccountId(), Type.SELL);

		accountService.updateAccountAfterTrade(buyAccountParams);
		accountService.updateAccountAfterTrade(sellAccountParams);
		
		// 보유 내역 업데이트
		final AfterTradeHoldingsDto buyHoldingsDto =
				holdingsMapper.toAfterTradeHoldingsDto(event, event.buyAccountId(), Type.BUY);
		final AfterTradeHoldingsDto sellHoldingsDto =
				holdingsMapper.toAfterTradeHoldingsDto(event, event.sellAccountId(), Type.SELL);

		holdingsService.updateHoldingsAfterTrade(buyHoldingsDto);
		holdingsService.updateHoldingsAfterTrade(sellHoldingsDto);
	}
}
