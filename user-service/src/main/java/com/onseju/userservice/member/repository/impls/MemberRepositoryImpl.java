package com.onseju.userservice.member.repository.impls;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.onseju.userservice.member.domain.Member;
import com.onseju.userservice.member.repository.MemberJpaRepository;
import com.onseju.userservice.member.service.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository memberJpaRepository;

	@Override
	public Optional<Member> findByEmail(final String email) {
		return memberJpaRepository.findByEmail(email);
	}

	@Override
	public Member save(final Member member) {
		return memberJpaRepository.save(member);
	}
}
