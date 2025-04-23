package com.onseju.userservice.member.service.repository;

import java.util.Optional;

import com.onseju.userservice.member.domain.Member;

public interface MemberRepository {

	Optional<Member> findByEmail(final String email);

	Member save(final Member member);
}
