package ru.shutoff.messenger.controller;

import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.dto.LoginRequest;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.dto.UserSecondaryInfoDTO;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.security.JwtUtils;
import ru.shutoff.messenger.service.AuthApiService;

@RestController
@RequestMapping("/authApi")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthApiService service;
    private final JwtUtils jwtUtils;

    @PostMapping("/user")
    public User registerUser(@Valid @RequestBody UserPrimaryInfoDTO dto) {
        return service.register(dto.email(), dto.login(), dto.password());
    }

    @GetMapping("/user")
    public User endRegistration(@Valid @NotNull @NotBlank @NotEmpty @RequestParam String token, HttpServletResponse response) {
        Pair<User, String> pair = service.endRegistration(token);
        Cookie cookie = new Cookie("JwtToken", pair.getSecond());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return pair.getFirst();
    }

    @PatchMapping("/user")
    public User updateUser(
            @Valid @RequestBody UserSecondaryInfoDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
        User user = service.updateUser(cookie.getValue(), dto.description(), dto.phoneNumber(), dto.urlTag());
        jwtUtils.refreshJwtToken(cookie.getValue(), cookie);
        response.addCookie(cookie);
        return user;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = jwtUtils.getJwtCookieFromRequest(request);
        service.logout(cookie);
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = service.login(loginRequest.login(), loginRequest.password());
        Cookie cookie = new Cookie("JwtToken", token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return token;
    }
}