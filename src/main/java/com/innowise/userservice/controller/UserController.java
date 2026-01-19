package com.innowise.userservice.controller;

import com.innowise.userservice.constant.UserApi;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserApi.BASE)
@RequiredArgsConstructor
public class UserController {

  private final UserMapper userMapper;
  private final UserServiceImpl userServiceImpl;

  @PostMapping()
  public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
    UserDto createdDto = userServiceImpl.create(userDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
  }

  @GetMapping(UserApi.ID)
  public ResponseEntity<UserDto> getById(@PathVariable(name = "id") Long id) {
    UserDto userDto = userServiceImpl.getById(id);
    return ResponseEntity.ok(userDto);
  }

  @GetMapping
  public ResponseEntity<Page<UserDto>> getAll(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      @RequestParam(name = "name", defaultValue = "") String name,
      @RequestParam(name = "surname", defaultValue = "") String surname) {
    Page<UserDto> pages = userServiceImpl.getAll(page, size, name, surname);
    return ResponseEntity.ok(pages);
  }

  @PostMapping(UserApi.ID)
  public ResponseEntity<UserDto> update(@PathVariable(name = "id") Long id,
      @Validated @RequestBody UserDto userDto) {

    UserDto updatedUserDto = userServiceImpl.updateById(id, userDto);
    return ResponseEntity.ok(updatedUserDto);
  }

  @PostMapping(UserApi.ACTIVATE)
  public ResponseEntity<UserDto> activate(@PathVariable(name = "id") Long id) {
    UserDto activatedUser = userServiceImpl.activate(id);
    return ResponseEntity.ok(activatedUser);
  }

  @PostMapping(UserApi.DEACTIVATE)
  public ResponseEntity<UserDto> deactivate(@PathVariable(name = "id") Long id) {
    UserDto deactivatedUser = userServiceImpl.deactivate(id);
    return ResponseEntity.ok(deactivatedUser);
  }
}
