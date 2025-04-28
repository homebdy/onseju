package com.onseju.userservice.member.repository.impls;

import com.onseju.userservice.member.domain.Member;
import com.onseju.userservice.member.exception.MemberNotFoundException;
import com.onseju.userservice.member.repository.MemberJpaRepository;
import com.onseju.userservice.member.service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

	@Override
	public Member findByUsername(String username) {
		return memberJpaRepository.findByUsername(username)
				.orElseThrow(MemberNotFoundException::new);
	}
}
