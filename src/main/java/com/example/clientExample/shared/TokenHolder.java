package com.example.clientExample.shared;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenHolder {

    private final AtomicReference<String> token = new AtomicReference<>();

    public String get() {
        return token.get();
    }

    public void set(String token) {
        this.token.set(token);
    }
}