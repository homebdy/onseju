package com.onseju.orderservice.global.config;

import com.onseju.orderservice.grpc.OrderValidationServiceGrpc;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration({
        net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration.class,
        net.devh.boot.grpc.client.autoconfigure.GrpcClientMetricAutoConfiguration.class,
        net.devh.boot.grpc.client.autoconfigure.GrpcClientHealthAutoConfiguration.class,
        net.devh.boot.grpc.client.autoconfigure.GrpcClientSecurityAutoConfiguration.class,
        net.devh.boot.grpc.client.autoconfigure.GrpcDiscoveryClientAutoConfiguration.class,
        net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration.class,
})
public class GrpcConfig {

    @Bean
    public OrderValidationServiceGrpc.OrderValidationServiceBlockingStub orderValidationStub(
            GrpcChannelFactory grpcChannelFactory) {
        return OrderValidationServiceGrpc.newBlockingStub(
                grpcChannelFactory.createChannel("user-service"));
    }
}

