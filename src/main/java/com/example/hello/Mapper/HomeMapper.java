package com.example.hello.Mapper;

import com.example.hello.DataProjection.HomeInfo;
import com.example.hello.Feature.User.DTO.HomeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HomeMapper {
    HomeResponse toHomeResponse(HomeInfo homeInfo);
}
