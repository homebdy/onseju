package com.onseju.userservice.global.config;

import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration({
        net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration.class,
        net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration.class,
        net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration.class,
        net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration.class
})
public class GrpcConfig {
    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new BasicGrpcAuthenticationReader();
    }
}

