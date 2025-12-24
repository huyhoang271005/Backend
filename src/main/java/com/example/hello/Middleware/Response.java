package com.example.hello.Middleware;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Response <T>{
    Boolean success;
    String message;
    T data;
}
