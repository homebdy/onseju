package com.onseju.orderservice.order.controller;

import com.onseju.orderservice.global.response.ApiResponse;
import com.onseju.orderservice.global.security.UserDetailsImpl;
import com.onseju.orderservice.order.controller.request.OrderRequest;
import com.onseju.orderservice.order.controller.resposne.OrderResponse;
import com.onseju.orderservice.order.service.OrderService;
import com.onseju.orderservice.order.service.dto.OrderCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> received(
            @RequestBody final OrderRequest request,
            @AuthenticationPrincipal final UserDetailsImpl user
    ) {
        return new ApiResponse<>(
                "주문 접수 성공",
                orderService.placeOrder(
                        new OrderCreateCommand(
                                request.companyCode(),
                                request.type(),
                                request.totalQuantity(),
                                request.price(),
                                user.getUsername()
                        )
                ),
                HttpStatus.OK.value()
        );
    }
}
