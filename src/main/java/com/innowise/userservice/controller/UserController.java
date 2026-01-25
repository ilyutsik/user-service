package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.UserActivePatchDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserApi.BASE)
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping()
  public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
    UserDto createdDto = userService.create(userDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
  }

  @GetMapping(UserApi.ID)
  public ResponseEntity<UserDto> getById(@PathVariable(name = "id") Long id) {
    UserDto userDto = userService.getById(id);
    return ResponseEntity.ok(userDto);
  }

  @GetMapping
  public ResponseEntity<Page<UserDto>> getAll(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      @RequestParam(name = "name", defaultValue = "") String name,
      @RequestParam(name = "surname", defaultValue = "") String surname) {
    Page<UserDto> pages = userService.getAll(page, size, name, surname);
    return ResponseEntity.ok(pages);
  }

  @PutMapping(UserApi.ID)
  public ResponseEntity<UserDto> update(@PathVariable(name = "id") Long id,
      @Validated @RequestBody UserDto userDto) {

    UserDto updatedUserDto = userService.updateById(id, userDto);
    return ResponseEntity.ok(updatedUserDto);
  }

  @GetMapping(UserApi.CARDS)
  public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(
      @PathVariable(name = "id") Long id) {
    List<PaymentCardDto> cards = userService.getCardsByUserId(id);
    return ResponseEntity.ok(cards);
  }

  @PatchMapping(UserApi.ID)
  public ResponseEntity<UserDto> active(@PathVariable(name = "id") Long id,
      @Valid @RequestBody UserActivePatchDto dto) {
    UserDto activatedUser = userService.setActive(id, dto.getActive());
    return ResponseEntity.ok(activatedUser);
  }

  @DeleteMapping(UserApi.ID)
  public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
