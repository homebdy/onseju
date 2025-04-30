package com.onseju.orderservice.events.listener;

import com.onseju.orderservice.chart.service.ChartService;
import com.onseju.orderservice.events.dto.MatchedEvent;
import com.onseju.orderservice.events.dto.OrderBookSyncedEvent;
import com.onseju.orderservice.events.mapper.EventMapper;
import com.onseju.orderservice.global.config.RabbitMQConfig;
import com.onseju.orderservice.order.service.OrderService;
import com.onseju.orderservice.tradehistory.domain.TradeHistory;
import com.onseju.orderservice.tradehistory.service.TradeHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 주문 서비스의 체결 이벤트 리스너
 * RabbitMQ를 통해 수신된 이벤트를 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchedEventListener {

    private final TradeHistoryService tradeHistoryService;
    private final OrderService orderService;
    private final ChartService chartService;
    //	private final TradeHistoryNotificationService tradeHistoryNotificationService;
    private final EventMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 주문 매칭 이벤트 처리
     * 매칭 엔진에서 매칭이 발생하면 거래 내역 생성
     */
    @RabbitListener(queues = RabbitMQConfig.MATCHING_RESULT_QUEUE)
    public void handleOrderMatched(final MatchedEvent event) {
        // 체결 내역 저장
        TradeHistory tradeHistory = tradeHistoryService.save(event);

        // 메모리에 거래 내역 저장 및 차트 업데이트
        chartService.processNewTrade(tradeHistory);

        // 주문 내역에서 남은 양 차감
        orderService.updateRemainingQuantity(mapper.toMatchedOrderUpdateDto(event.buyOrderId(), event));
        orderService.updateRemainingQuantity(mapper.toMatchedOrderUpdateDto(event.sellOrderId(), event));

        // 사용자에게 체결 완료 알람 발송
//		tradeHistoryNotificationService.sendNotification(event);
    }

    /**
     * 호가창 이벤트 처리
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_BOOK_SYNCED_QUEUE)
    public void handleOrderBookSynced(final OrderBookSyncedEvent event) {
        messagingTemplate.convertAndSend("/topic/orderbook/" + event.companyCode(), event);
    }
}
