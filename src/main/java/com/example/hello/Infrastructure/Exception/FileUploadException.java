package com.example.hello.Infrastructure.Exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
}
