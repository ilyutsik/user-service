package org.innowise.userservice.service;

import org.innowise.userservice.model.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto getById(Long id);

    Page<UserDto> getAll(int page, int size, String name, String surname);

    UserDto updateById(Long id, UserDto newUserDto);

    UserDto activate(Long id);

    UserDto deactivate(Long id);
}
