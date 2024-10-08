package ru.shutoff.messenger.domain.user_mgmt.controller;

import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.dto.LoginRequest;
import ru.shutoff.messenger.domain.user_mgmt.dto.RegisterRequest;
import ru.shutoff.messenger.domain.user_mgmt.dto.UpdateInfoRequest;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.security.JwtUtils;
import ru.shutoff.messenger.domain.user_mgmt.service.AuthApiService;

@RestController
@RequestMapping("/authApi")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthApiService service;
    private final JwtUtils jwtUtils;

    @PostMapping("/user")
    public User registerUser(@Valid @RequestBody RegisterRequest dto) {
        return service.register(dto.email(), dto.login(), dto.name(), dto.password());
    }

    @GetMapping("/user")
    public User endRegistration(@Valid @NotNull @NotBlank @NotEmpty @RequestParam String token, HttpServletResponse response) {
        Pair<User, String> pair = service.endRegistration(token);
        response.addCookie(jwtUtils.formCookie(pair.getSecond()));
        return pair.getFirst();
    }

    @PatchMapping("/user")
    public User updateUser(
            @Valid @RequestBody UpdateInfoRequest dto,
            @CookieValue(name = JwtUtils.JwtCookieName, required = true) Cookie cookie,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user = service.updateUser(cookie.getValue(), dto.description(), dto.phoneNumber(), dto.urlTag());
        jwtUtils.refreshJwtToken(cookie);
        response.addCookie(cookie);
        return user;
    }

    @GetMapping("/logout")
    public void logout(@CookieValue(name = JwtUtils.JwtCookieName, required = true) Cookie cookie, HttpServletResponse response) {
        service.logout(cookie);
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = service.login(loginRequest.login(), loginRequest.password());
        response.addCookie(jwtUtils.formCookie(token));
        return token;
    }
}