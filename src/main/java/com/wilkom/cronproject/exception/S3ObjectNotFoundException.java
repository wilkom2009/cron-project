package com.wilkom.cronproject.exception;

public class S3ObjectNotFoundException extends Exception {
    public S3ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
