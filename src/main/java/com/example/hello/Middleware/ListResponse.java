package com.example.hello.Middleware;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListResponse<T> {
    Boolean hasMore;
    List<T> listData;
}
