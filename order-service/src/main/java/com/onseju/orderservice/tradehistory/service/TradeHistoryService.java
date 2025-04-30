package com.onseju.orderservice.tradehistory.service;

import com.onseju.orderservice.events.dto.MatchedEvent;
import com.onseju.orderservice.order.domain.Order;
import com.onseju.orderservice.order.service.repository.OrderRepository;
import com.onseju.orderservice.tradehistory.domain.TradeHistory;
import com.onseju.orderservice.tradehistory.dto.TradeHistoryResponse;
import com.onseju.orderservice.tradehistory.mapper.TradeHistoryMapper;
import com.onseju.orderservice.tradehistory.service.repository.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 거래 내역 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class TradeHistoryService {

    private final TradeHistoryRepository tradeHistoryRepository;
    private final OrderRepository orderRepository;
    private final TradeHistoryMapper tradeHistoryMapper;

    /**
     * 거래 내역 저장
     */
    @Transactional
    public TradeHistory save(final MatchedEvent event) {
        return tradeHistoryRepository.save(tradeHistoryMapper.toEntity(event));
    }

    @Transactional(readOnly = true)
    public Collection<TradeHistoryResponse> getAllTradeHistory(Long memberId) {
        List<Order> orders = orderRepository.findByMemberId(memberId);
        return orders.stream()
                .map(order ->
                        tradeHistoryRepository.findByOrderId(order).stream()
                                .map(tradeHistory -> tradeHistoryMapper.toResponse(tradeHistory, order))
                                .toList()
                )
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toList();
    }
}
