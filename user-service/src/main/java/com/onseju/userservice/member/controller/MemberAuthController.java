package com.onseju.userservice.member.controller;

import com.onseju.userservice.member.controller.response.LoginResponseDto;
import com.onseju.userservice.member.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberAuthController {
	private final GoogleOAuthService googleOAuthService;

	@Value("${google.client.id}")
	private String googleClientId;

	@Value("${google.redirect.uri}")
	private String googleRedirectUri;

	@GetMapping("/google/login")
	public void redirectToGoogle(HttpServletResponse response) throws IOException {
		String googleLoginUrl = "https://accounts.google.com/o/oauth2/auth"
				+ "?client_id=" + googleClientId
				+ "&redirect_uri=" + googleRedirectUri
				+ "&response_type=code"
				+ "&scope=email%20profile";
		log.info("google login url : {}", googleLoginUrl);
		response.sendRedirect(googleLoginUrl);
	}

	@GetMapping("/google/callback")
	public ResponseEntity<LoginResponseDto> googleLogin(@RequestParam("code") String code,
														HttpServletResponse response) throws IOException {
		LoginResponseDto responseDto = googleOAuthService.googleLogin(code, response);
		return ResponseEntity.ok().body(responseDto);
	}
}
