package org.innowise.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.model.entity.User;
import org.innowise.userservice.service.impl.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(ApiConstant.USERS_BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserServiceImpl userServiceImpl;

    private UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    @PostMapping()
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        UserDto createdDto = userServiceImpl.create(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @GetMapping(ApiConstant.USER_ID_PATH)
    public ResponseEntity<UserDto> getById(@PathVariable(name = "id") Long id) {
        UserDto userDto = userServiceImpl.getById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(@RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                @RequestParam(name = "name", defaultValue = "") String name,
                                                @RequestParam(name = "surname", defaultValue = "") String surname) {

        Page<UserDto> pages = userServiceImpl.getAll(page, size, name, surname);
        return ResponseEntity.ok(pages);
    }

    @PostMapping(ApiConstant.USER_ID_PATH)
    public ResponseEntity<UserDto> update(@PathVariable(name = "id") Long id,
                                          @Validated @RequestBody UserDto userDto) {

        UserDto updatedUserDto = userServiceImpl.updateById(id, userDto);
        return ResponseEntity.ok(updatedUserDto);
    }

    @PostMapping(ApiConstant.ACTIVATE_USER)
    public ResponseEntity<UserDto> activate(@PathVariable(name = "id") Long id) {
        UserDto activatedUser = userServiceImpl.activate(id);
        return ResponseEntity.ok(activatedUser);
    }

    @PostMapping(ApiConstant.DEACTIVATE_USER)
    public ResponseEntity<UserDto> deactivate(@PathVariable(name = "id") Long id) {
        UserDto deactivatedUser = userServiceImpl.deactivate(id);
        return ResponseEntity.ok(deactivatedUser);
    }

}
