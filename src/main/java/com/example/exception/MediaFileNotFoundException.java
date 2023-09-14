package com.example.exception;

public class MediaFileNotFoundException extends RuntimeException {

    public MediaFileNotFoundException(String message) {
        super(message);
    }

}
