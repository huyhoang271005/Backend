package com.example.hello.Users.User.Mapper;

import com.example.hello.Users.User.DTO.RegisterRequest;
import com.example.hello.Users.User.Entity.Profile;
import com.example.hello.Users.User.Entity.User;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T00:02:54+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUsers(RegisterRequest registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( registerRequest.getUsername() );
        user.password( registerRequest.getPassword() );

        return user.build();
    }

    @Override
    public Profile toUserProfile(RegisterRequest registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        Profile.ProfileBuilder profile = Profile.builder();

        profile.fullName( registerRequest.getFullName() );
        if ( registerRequest.getBirthday() != null ) {
            profile.birthday( LocalDate.parse( registerRequest.getBirthday() ) );
        }
        profile.gender( registerRequest.getGender() );

        return profile.build();
    }
}
