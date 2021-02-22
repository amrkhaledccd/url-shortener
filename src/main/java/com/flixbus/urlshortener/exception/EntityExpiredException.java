package com.flixbus.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EntityExpiredException extends RuntimeException {
    public EntityExpiredException(String message) {
        super(message);
    }
}
