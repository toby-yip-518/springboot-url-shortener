package com.toby.urlshortener.exception;

public class ShortCodeNotFoundException extends RuntimeException {

    public ShortCodeNotFoundException(String message) {
        super(message);
    }
}