package com.example.hello.Mapper;

import com.example.hello.DataProjection.SessionInfo;
import com.example.hello.Entity.Session;
import com.example.hello.Feature.User.DTO.SessionResponse;
import com.example.hello.Feature.User.DTO.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionResponse toSessionResponse(SessionInfo sessionInfo);
    Address toAddress(SessionInfo sessionInfo);
    void updateSession(Address address, @MappingTarget Session session);
}
