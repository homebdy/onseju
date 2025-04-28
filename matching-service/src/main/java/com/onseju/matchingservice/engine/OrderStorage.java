package com.onseju.matchingservice.engine;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.events.MatchedEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderStorage {

    private final ConcurrentSkipListSet<TradeOrder> elements = new ConcurrentSkipListSet<>(
            Comparator.comparing(TradeOrder::getTimestamp)
                    .thenComparing(TradeOrder::getTotalQuantity, Comparator.reverseOrder())
                    .thenComparing(TradeOrder::getId)
    );

    // Set 내에 존재하는 주문과 입력된 주문을 매칭힌다.
    public List<MatchedEvent> match(final TradeOrder incomingOrder) {
        Iterator<TradeOrder> iterator = elements.iterator();
        List<MatchedEvent> results = new ArrayList<>();
        while (iterator.hasNext() && incomingOrder.hasRemainingQuantity()) {
            final TradeOrder foundOrder = iterator.next();
            if (foundOrder.isSameAccount(incomingOrder.getAccountId())) {
                continue;
            }

            BigDecimal matchedQuantity = incomingOrder.calculateMatchQuantity(foundOrder);
            updateRemainingQuantity(incomingOrder, foundOrder, matchedQuantity);
            results.add(MatchedEvent.of(incomingOrder, foundOrder, matchedQuantity));
            if (!foundOrder.hasRemainingQuantity()) {
                iterator.remove();
            }
        }
        return results;
    }

    // 체결 완료 후 남은 수량 감소 및 완료 여부 확인
    private void updateRemainingQuantity(
            final TradeOrder incomingOrder,
            final TradeOrder foundedOrder,
            final BigDecimal matchedQuantity
    ) {
        incomingOrder.decreaseRemainingQuantity(matchedQuantity);
        foundedOrder.decreaseRemainingQuantity(matchedQuantity);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void add(TradeOrder order) {
        elements.add(order);
    }
}
