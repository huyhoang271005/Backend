package com.example.hello.Infrastructure.Exception;

public class FileUploadIOException extends RuntimeException {
    public FileUploadIOException(String message) {
        super(message);
    }
}
