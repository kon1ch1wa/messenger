package ru.shutoff.messenger.domain.user_mgmt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.service.ForgotCredsService;

@RestController
@RequestMapping("/forgotCredsApi")
@RequiredArgsConstructor
public class ForgotCredsController {
	private final ForgotCredsService service;

	@PostMapping("/forgotLogin")
	public String forgotLogin(@Valid @RequestBody String email) {
		return service.forgotLogin(email);
	}

	@GetMapping("/checkLogin")
	public String checkLogin(@Valid @RequestParam String key) {
		return service.checkLogin(key);
	}

	@PostMapping("/forgotPassword")
	public String forgotPassword(@Valid @RequestBody String login) {
		return service.forgotPassword(login);
	}
}
