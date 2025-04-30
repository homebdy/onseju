package com.onseju.matchingservice.integration.rabbitmq;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.events.dto.CreatedOrderEvent;
import com.onseju.matchingservice.events.listener.CreatedOrderEventListener;
import com.onseju.matchingservice.service.MatchingService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class OrderedEventHandlerTest {

    @Autowired
    CreatedOrderEventListener createdOrderEventListener;

    @Autowired
    MatchingService matchingService;

    @Test
    @DisplayName("이벤트를 전달받아 비동기로 처리한다.")
    void handleOrderEventShouldProcessOrder() {
        // given
        CreatedOrderEvent orderedEvent = new CreatedOrderEvent(
                UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
                1L,
                "005930",
                Type.LIMIT_BUY,
                OrderStatus.ACTIVE,
                new BigDecimal(100),
                new BigDecimal(100),
                new BigDecimal(100),
                Instant.now().toEpochMilli(),
                1L
        );

        // when
        CompletableFuture.runAsync(() -> createdOrderEventListener.handleOrderEvent(orderedEvent))
                .orTimeout(2, TimeUnit.SECONDS) // 비동기 실행을 기다림
                .join();

        // then
        Assertions.assertThatCode(() -> matchingService.matchOrder(orderedEvent))
                .doesNotThrowAnyException();
    }
}
