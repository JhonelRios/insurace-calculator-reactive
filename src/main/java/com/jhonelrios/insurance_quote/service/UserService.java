package com.jhonelrios.insurance_quote.service;

import com.jhonelrios.insurance_quote.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> findByUsername(String username);
    Mono<Boolean> validateCredentials(String username, String password);
    Mono<Void> createUser(String username, String password);
}
