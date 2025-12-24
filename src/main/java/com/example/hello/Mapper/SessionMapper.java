package com.example.hello.Mapper;

import com.example.hello.DataProjection.SessionInfo;
import com.example.hello.Feature.User.DTO.SessionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionResponse toSessionResponse(SessionInfo sessionInfo);
}
