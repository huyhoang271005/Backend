package com.example.hello.Infrastructure.Exception;

import com.example.hello.Middleware.StringApplication;
import com.example.hello.Middleware.Response;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage()))
                ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleBadRequestException(BadRequestException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleInvalidFormatException(InvalidFormatException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response<ErrorResponse>> handleUnauthorizedException(UnauthorizedException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new Response<>(false, e.getMessage(),
                        new ErrorResponse(e.getMessage())
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse> errorMessages = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            log.error(error.getDefaultMessage());
            if(error.getDefaultMessage() != null)
                errorMessages.add(new ErrorResponse(error.getDefaultMessage()));
        });
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new Response<>(false,
                        StringApplication.ERROR.INPUT_INVALID,
                        errorMessages
        ));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleUnprocessableEntity(UnprocessableEntityException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleConflict(ConflictException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleFileUpload(FileUploadException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage())))
        );
    }

    @ExceptionHandler(FileUploadIOException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleFileUploadIOException(FileUploadIOException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage()))
                        )
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleAuthorizationDenied(AuthorizationDeniedException e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new Response<>(false, StringApplication.ERROR.FORBIDDEN,
                        List.of(new ErrorResponse(e.getMessage()))
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleException(Exception e) {
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new Response<>(false, e.getMessage(),
                        List.of(new ErrorResponse(e.getMessage()))
                ));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<List<ErrorResponse>>> handleGeneric(Exception ex) {
        log.error(ex.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>(false, ex.getMessage(),
                        List.of(new ErrorResponse(ex.getMessage()))
                        ));
    }
}
