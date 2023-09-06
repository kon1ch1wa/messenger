package ru.shutoff.messenger.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.dto.RestorePasswordNoAccessDto;
import ru.shutoff.messenger.dto.RestorePasswordWithAccessDto;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.security.JwtUtils;
import ru.shutoff.messenger.service.UpdateCredsService;

@RestController
@RequestMapping("/updateCredsApi")
@RequiredArgsConstructor
public class UpdateCredsController {
	private final UpdateCredsService service;
	private final JwtUtils jwtUtils;

	@PostMapping("/restorePassword")
	public User restorePasswordNoAccess(@RequestParam String key, @RequestBody RestorePasswordNoAccessDto dto) {
		return service.restorePasswordThroughKey(key, dto.password(), dto.passwordConfirm());
	}

	@PatchMapping("/restorePassword")
	public User restorePasswordWithAccess(@RequestBody RestorePasswordWithAccessDto dto, HttpServletRequest request) {
		Cookie jwtCookie = jwtUtils.getJwtCookieFromRequest(request);
		return service.restorePasswordThroughJwt(jwtCookie, dto.newPassword(), dto.newPasswordConfirm(), dto.oldPassword());
	}
}
