package com.onseju.userservice.events.listener;

import com.onseju.userservice.account.service.AccountService;
import com.onseju.userservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.userservice.global.config.RabbitMQConfig;
import com.onseju.userservice.holding.service.HoldingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 사용자 서비스의 이벤트 리스너
 * RabbitMQ를 통해 수신된 이벤트를 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchedEventListener {
    private final AccountService accountService;
    private final HoldingsService holdingsService;

    /**
     * 주문 매칭 이벤트 처리
     */
    @RabbitListener(queues = RabbitMQConfig.USER_UPDATE_QUEUE)
    public void handleOrderMatched(final MatchedOrderUpdateEvent event) {
        accountService.updateAccountAfterTrade(event);
        holdingsService.updateHoldingsAfterTrade(event);
    }
}
