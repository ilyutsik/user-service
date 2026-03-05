package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.UserDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface UserService {

  UserDto create(UserDto userDto);

  UserDto getById(Long id);

  UserDto getByEmail(String email);

  Page<UserDto> getAll(int page, int size, String name, String surname);

  UserDto updateById(Long id, UserDto newUserDto);

  List<PaymentCardDto> getCardsByUserId(Long userId);

  UserDto setActive(Long id, boolean active);

  void delete(Long id);
}
