package ru.shutoff.messenger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.security.JwtUtils;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PingController {
	private final JwtUtils jwtUtils;

	@GetMapping("/ping")
	public String ping(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
		jwtUtils.refreshJwtToken(cookie.getValue(), cookie);
		response.addCookie(cookie);
		return "ping";
	}
}
