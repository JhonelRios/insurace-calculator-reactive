package com.jhonelrios.insurance_quote.controller;

import com.jhonelrios.insurance_quote.dto.AuthDTO;
import com.jhonelrios.insurance_quote.service.UserService;
import com.jhonelrios.insurance_quote.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public Mono<Map<String, String>> login(@RequestBody AuthDTO request) {
        String username = request.getUsername();
        String password = request.getPassword();

        return userService.validateCredentials(username, password)
                .flatMap(isValid -> {
                    if (isValid) {
                        String token = jwtUtil.generateToken(username);
                        return Mono.just(Map.of("token", token));
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
                    }
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username or password")));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@RequestBody AuthDTO request) {
        String username = request.getUsername();
        String password = request.getPassword();

        return userService.createUser(username, password);
    }
}
