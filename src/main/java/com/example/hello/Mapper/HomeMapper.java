package com.example.hello.Mapper;

import com.example.hello.Feature.User.dto.HomeInfo;
import com.example.hello.Feature.User.dto.HomeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HomeMapper {
    HomeResponse toHomeResponse(HomeInfo homeInfo);
}
