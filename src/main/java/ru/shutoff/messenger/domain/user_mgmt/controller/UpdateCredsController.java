package ru.shutoff.messenger.domain.user_mgmt.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.dto.RestorePasswordNoAccessDto;
import ru.shutoff.messenger.domain.user_mgmt.dto.RestorePasswordWithAccessDto;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.security.JwtUtils;
import ru.shutoff.messenger.domain.user_mgmt.service.UpdateCredsService;

@RestController
@RequestMapping("/updateCredsApi")
@RequiredArgsConstructor
public class UpdateCredsController {
	private final UpdateCredsService service;

	@PostMapping("/restorePassword")
	public User restorePasswordNoAccess(@RequestParam String key, @Valid @RequestBody RestorePasswordNoAccessDto dto) {
		return service.restorePasswordThroughKey(key, dto.password(), dto.passwordConfirm());
	}

	@PatchMapping("/restorePassword")
	public User restorePasswordWithAccess(
		@Valid @RequestBody RestorePasswordWithAccessDto dto,
		@CookieValue(name = JwtUtils.JwtCookieName, required = false) Cookie cookie,
		HttpServletRequest request
	) {
		return service.restorePasswordThroughJwt(cookie, dto.newPassword(), dto.newPasswordConfirm(), dto.oldPassword());
	}
}
