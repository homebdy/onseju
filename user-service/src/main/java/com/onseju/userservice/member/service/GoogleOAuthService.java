package com.onseju.userservice.member.service;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.onseju.userservice.global.jwt.JwtUtil;
import com.onseju.userservice.member.controller.response.LoginResponseDto;
import com.onseju.userservice.member.domain.Member;
import com.onseju.userservice.member.domain.Role;
import com.onseju.userservice.member.service.repository.MemberRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class GoogleOAuthService {
	private final MemberRepository memberRepository;
	private final JwtUtil jwtUtil;
	private final RestTemplate restTemplate;

	@Value("${google.client.id}")
	private String clientId;

	@Value("${google.client.secret}")
	private String clientSecret;

	@Value("${google.redirect.uri}")
	private String redirectUri;

	@Value("${google.token.uri}")
	private String tokenUri;

	@Value("${google.userinfo.uri}")
	private String userInfoUri;

	public GoogleOAuthService(MemberRepository memberRepository, JwtUtil jwtUtil, RestTemplate restTemplate) {
		this.memberRepository = memberRepository;
		this.jwtUtil = jwtUtil;
		this.restTemplate = restTemplate;
	}

	public LoginResponseDto googleLogin(String code, HttpServletResponse response) throws IOException {
		// Step 1: Exchange code for access token
		String accessToken = getAccessToken(code);

		// Step 2: Get user info using the access token
		JsonNode userInfo = getUserInfo(accessToken);
		String email = userInfo.get("email").asText();
		String googleId = userInfo.get("sub").asText();  // Google ID
		String username = email.split("@")[0]; // Extract username from email

		// Step 3: Find or Register User and Account
		Member user = memberRepository.findByEmail(email)
				.orElseGet(() -> {
					// Create new user if not exists
					Member newUser = Member.builder()
							.username(username)
							.email(email)
							.googleId(googleId)
							.role(Role.USER)
							.build();
					newUser.createAccount();
					return memberRepository.save(newUser);
				});

		// Step 4: Generate JWT token
		String jwtToken = jwtUtil.createToken(user.getId(), user.getUsername());
		response.addHeader("Authorization", jwtToken);
		return new LoginResponseDto(user.getId(), username, user.getMemberBalance());
	}

	private String getAccessToken(String code) {
		URI uri = UriComponentsBuilder.fromUriString(tokenUri)
				.queryParam("code", code)
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("grant_type", "authorization_code")
				.build().toUri();

		ResponseEntity<JsonNode> response = restTemplate.postForEntity(uri, null, JsonNode.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody().get("access_token").asText();
		} else {
			throw new RuntimeException("Failed to retrieve access token from Google");
		}
	}

	private JsonNode getUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<JsonNode> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, JsonNode.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new RuntimeException("Failed to retrieve user info from Google");
		}
	}
}
