package com.example.hello.Mapper;

import com.example.hello.Entity.Profile;
import com.example.hello.Entity.User;
import com.example.hello.Feature.User.DTO.ProfileResponse;
import com.example.hello.Feature.User.DTO.RegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterRequest registerRequest);
    Profile toProfile(RegisterRequest registerRequest);
    ProfileResponse toProfileResponse(Profile profile);
}
