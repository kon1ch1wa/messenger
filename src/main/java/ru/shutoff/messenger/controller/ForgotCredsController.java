package ru.shutoff.messenger.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.service.ForgotCredsService;

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
