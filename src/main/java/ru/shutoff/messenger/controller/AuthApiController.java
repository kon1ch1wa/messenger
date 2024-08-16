package ru.shutoff.messenger.controller;

import org.springframework.data.util.Pair;
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
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.dto.LoginRequest;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.dto.UserSecondaryInfoDTO;
import ru.shutoff.messenger.exception.NotAuthorizedException;
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
    public User registerUser(@RequestBody UserPrimaryInfoDTO dto) {
        return service.register(dto.email(), dto.login(), dto.password());
    }

    @GetMapping("/user")
    public User endRegistration(@RequestParam String token, HttpServletResponse response) {
        Pair<User, String> pair = service.endRegistration(token);
        Cookie cookie = new Cookie("JwtToken", pair.getSecond());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return pair.getFirst();
    }

    @PatchMapping("/user")
    public User updateUser(
            @RequestBody UserSecondaryInfoDTO dto,
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
        if (cookie == null) {
            throw new NotAuthorizedException("Not Authorized to logout");
        }
        service.logout(cookie);
        //response.setHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue());
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String token = service.login(loginRequest.login(), loginRequest.password());
        Cookie cookie = new Cookie("JwtToken", token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return token;
    }
}