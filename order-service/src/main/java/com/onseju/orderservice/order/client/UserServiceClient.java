package com.onseju.orderservice.order.client;

import com.onseju.orderservice.grpc.GrpcValidateRequest;
import com.onseju.orderservice.grpc.GrpcValidateResponse;
import com.onseju.orderservice.grpc.OrderValidationServiceGrpc;
import com.onseju.orderservice.order.dto.OrderValidationResponse;
import com.onseju.orderservice.order.service.dto.OrderCreateCommand;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserServiceClient {

    @GrpcClient("order-service")
    private OrderValidationServiceGrpc.OrderValidationServiceBlockingStub orderValidationServiceBlockingStub;

    public OrderValidationResponse validateOrder(OrderCreateCommand command) {

        try {
            GrpcValidateRequest request = GrpcValidateRequest.newBuilder()
                    .setCompanyCode(command.companyCode())
                    .setType(command.type().name())
                    .setTotalQuantity(command.totalQuantity().toPlainString())
                    .setPrice(command.price().toPlainString())
                    .setUsername(command.username())
                    .build();

            GrpcValidateResponse response = orderValidationServiceBlockingStub.validateOrder(request);

            // gRPC 응답을 ValidateResponse 객체로 변환
            OrderValidationResponse validateResponse = OrderValidationResponse.builder()
                    .memberId(response.getMemberId())
                    .result(response.getResult())
                    .build();

            return validateResponse;
        } catch (Exception e) {
            throw new RuntimeException("gRPC 서비스 통신 오류", e);
        }
    }


}
