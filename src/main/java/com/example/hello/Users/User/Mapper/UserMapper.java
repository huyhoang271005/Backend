package com.example.hello.Users.User.Mapper;

import com.example.hello.Users.User.Entity.Profile;
import com.example.hello.Users.User.Entity.User;
import com.example.hello.Users.User.DTO.RegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUsers(RegisterRequest registerRequest);
    Profile toUserProfile(RegisterRequest registerRequest);
}
