package com.onseju.matchingservice.service;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.MatchingEngine;
import com.onseju.matchingservice.events.MatchedEvent;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.events.publisher.EventPublisher;
import com.onseju.matchingservice.mapper.MatchingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final MatchingEngine matchingEngine;
    private final MatchingMapper matchingMapper;
    private final EventPublisher<MatchedEvent> matchedEventPublisher;

    // 매칭 엔진으로 이벤트 전송 후, 매칭된 주문이 있을 경우 이벤트 발생
    public void matchOrder(final OrderCreatedEvent event) {
        final TradeOrder tradeOrder = matchingMapper.toTradeOrder(event);
        Collection<MatchedEvent> matchedEvents = matchingEngine.processOrder(tradeOrder);
        processMatchedEvents(matchedEvents);
    }

    // 모든 매칭 이벤트를 순회하며 개별 이벤트 처리
    private void processMatchedEvents(final Collection<MatchedEvent> events) {
        events.forEach(event -> {
            log.info("체결 완료: sell order - {}, buy order - {}",
                    event.sellOrderId(),
                    event.buyOrderId());
            matchedEventPublisher.publishEvent(event);
        });
    }
}
