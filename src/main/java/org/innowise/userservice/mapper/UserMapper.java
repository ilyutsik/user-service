package org.innowise.userservice.mapper;

import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "localDateToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToString")
    UserDto toDto(User user);

    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto userDto);

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null ? LocalDate.parse(date) : null;
    }


    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime date) {
        return date != null ? date.toString() : null;
    }

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String date) {
        return date != null ? LocalDateTime.parse(date) : null;
    }

}
