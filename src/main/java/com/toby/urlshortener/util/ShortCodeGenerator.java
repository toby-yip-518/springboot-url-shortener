package com.toby.urlshortener.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ShortCodeGenerator {

    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int LENGTH = 6;
    private final Random random = new Random();

    public String generate() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }
}