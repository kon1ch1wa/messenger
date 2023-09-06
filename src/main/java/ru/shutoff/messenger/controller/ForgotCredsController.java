package ru.shutoff.messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.service.ForgotCredsService;

@RestController
@RequestMapping("/forgotCredsApi")
@RequiredArgsConstructor
public class ForgotCredsController {
	private final ForgotCredsService service;

	@PostMapping("/forgotLogin")
	public String forgotLogin(@RequestBody String email) {
		return service.forgotLogin(email);
	}

	@GetMapping("/checkLogin")
	public String checkLogin(@RequestParam String key) {
		return service.checkLogin(key);
	}

	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestBody String login) {
		return service.forgotPassword(login);
	}
}
