package ru.shutoff.messenger.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shutoff.messenger.dto.UserPrimaryInfoDTO;
import ru.shutoff.messenger.service.AuthApiService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthApiService service;
    @PostMapping("/register")
    public void registerUser(@RequestBody UserPrimaryInfoDTO dto) {
        service.register(dto.email(), dto.login(), dto.password());
    }
}