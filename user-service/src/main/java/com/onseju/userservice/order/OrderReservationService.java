package com.onseju.userservice.order;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.exception.InsufficientBalanceException;
import com.onseju.userservice.account.mapper.AccountMapper;
import com.onseju.userservice.account.service.AccountService;
import com.onseju.userservice.grpc.GrpcValidateRequest;
import com.onseju.userservice.grpc.GrpcValidateResponse;
import com.onseju.userservice.grpc.OrderValidationServiceGrpc;
import com.onseju.userservice.holding.exception.HoldingsNotFoundException;
import com.onseju.userservice.holding.exception.InsufficientHoldingsException;
import com.onseju.userservice.holding.mapper.HoldingsMapper;
import com.onseju.userservice.holding.service.HoldingsService;
import com.onseju.userservice.member.service.repository.MemberRepository;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;


@GrpcService
@AllArgsConstructor
public class OrderReservationService extends OrderValidationServiceGrpc.OrderValidationServiceImplBase {

	private final AccountService accountService;
	private final AccountMapper accountMapper;

	private final HoldingsService holdingsService;
	private final HoldingsMapper holdingsMapper;

	private final MemberRepository memberRepository;

	@Override
	public void validateOrder(GrpcValidateRequest request, StreamObserver<GrpcValidateResponse> responseObserver) {
		try {
			// 1. grpc -> beforetradeorderdto
			CreatedOrderDto dto = convertToCreatedOrderDto(request);

			// 2. 검증
			Long memberId = memberRepository.findByUsername(dto.username()).getId();
			accountService.reserve(accountMapper.toCreatedOrderAccountUpdateDto(dto, dto.type(), memberId));
			holdingsService.reserve(holdingsMapper.toOrderCreatedHoldingsUpdateDto(dto, dto.type(), memberId));

			// 3. 검증 성공 응답 생성
			GrpcValidateResponse response = GrpcValidateResponse.newBuilder()
				.setMemberId(memberId)
				.setResult(true)
				.setMessage("검증 성공")
				.build();

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InsufficientBalanceException e) {
			// 잔액 부족 예외 처리
			handleValidationException(responseObserver, "잔액이 부족합니다", e);
		} catch (HoldingsNotFoundException e) {
			// 보유 주식 없음 예외 처리
			handleValidationException(responseObserver, "보유 주식이 없습니다", e);
		} catch (InsufficientHoldingsException e) {
			// 보유 주식 부족 예외 처리
			handleValidationException(responseObserver, "보유 주식이 충분하지 않습니다", e);
		} catch (Exception e) {
			// 기타 예외 처리
			handleValidationException(responseObserver, "주문 검증 중 오류 발생: " + e.getMessage(), e);
		}
	}

	/**
	 * 검증 예외 + 응답
	 */
	private void handleValidationException(
			StreamObserver<GrpcValidateResponse> responseObserver,
			String message,
			Exception e
	) {

		// 실패 응답 생성
		GrpcValidateResponse response = GrpcValidateResponse.newBuilder()
				.setMemberId(0L) // 실패 시 기본값
				.setResult(false)
				.setMessage(message)
				.build();

		// 응답 전송
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}



	private CreatedOrderDto convertToCreatedOrderDto(GrpcValidateRequest request) {
		return CreatedOrderDto.builder()
				.companyCode(request.getCompanyCode())
				.type(Type.valueOf(request.getType()))
				.totalQuantity(new BigDecimal(request.getTotalQuantity()))
				.price(new BigDecimal(request.getPrice()))
				.username(request.getUsername())
				.build();
	}
}
