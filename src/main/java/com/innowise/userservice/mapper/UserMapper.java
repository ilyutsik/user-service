package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toDto(User user);

  @Mapping(target = "cards", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toEntity(UserDto userDto);
}
