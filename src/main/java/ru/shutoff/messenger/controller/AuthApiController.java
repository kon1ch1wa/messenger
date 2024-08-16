package ru.shutoff.messenger.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.service.AuthApiService;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthApiService service;
    @PostMapping("/register")
    public User registerUser(@RequestBody UserPrimaryInfoDTO dto) {
        return service.register(dto.email(), dto.login(), dto.password());
    }

    @GetMapping("/endRegistration")
    public User endRegistration(@RequestParam String token) {
        return service.endRegistration(token);
    }
}