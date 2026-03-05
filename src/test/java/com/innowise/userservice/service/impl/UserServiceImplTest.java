package com.innowise.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Tag("unit")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PaymentCardRepository cardRepository;
  @Spy
  private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Spy
  private PaymentCardMapper cardMapper = Mappers.getMapper(PaymentCardMapper.class);
  @Mock
  private PaymentCardService paymentCardService;

  @InjectMocks
  private UserServiceImpl userService;

  private User user;
  private UserDto userDto;

  private Page<User> userPage;

  private PaymentCard paymentCard;

  private PaymentCard paymentCard2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    user = new User();
    user.setId(1L);
    user.setName("Andrei");
    user.setSurname("Ilyutsik");
    user.setEmail("test@mail.com");
    user.setBirthDate(LocalDate.of(2000, 1, 1));
    user.setActive(true);

    userDto = new UserDto();
    userDto.setId(1L);
    userDto.setName("Andrei");
    userDto.setSurname("Ilyutsik");
    userDto.setEmail("test@mail.com");
    userDto.setBirthDate(LocalDate.parse("2001-01-01"));
    userDto.setActive(true);

    paymentCard = new PaymentCard();
    paymentCard.setId(1L);
    paymentCard.setUser(user);

    paymentCard2 = new PaymentCard();
    paymentCard2.setId(1L);
    paymentCard2.setUser(user);

    List<User> userList = List.of(user);
    userPage = new PageImpl<>(userList, PageRequest.of(0, 10), userList.size());
  }

  @Test
  void create_WhenUserDoesNotExists_ShouldSaveAndReturnDto() {
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = userService.create(userDto);

    assertThat(result.getId()).isEqualTo(1L);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void create_WhenUserAlreadyExist_ShouldThrowException() {
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));

    Assertions.assertThrows(UserAlreadyExistsException.class,
        () -> userService.create(userDto));
  }

  @Test
  void getById_WhenExists_ShouldReturnUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserDto result = userService.getById(1L);

    assertThat(result.getEmail()).isEqualTo("test@mail.com");
  }

  @Test
  void getById_UserNotFond_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.getById(1L)).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void getByEmail_WhenExists_ShouldReturnUser() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    UserDto result = userService.getByEmail(user.getEmail());

    assertThat(result.getEmail()).isEqualTo("test@mail.com");
  }

  @Test
  void getByEmail_UserNotFond_ShouldThrowException() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.getByEmail(user.getEmail())).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void getAll_WhenUsersExists_ShouldReturnUsers() {
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(
        userPage);

    Page<UserDto> page = userService.getAll(0, 10, "", "");

    assertThat(page.get().findFirst().get().getSurname()).isEqualTo("Ilyutsik");
  }

  @Test
  void updateById_WhenUserExists_ShouldUpdateFields() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

    userDto.setName("Valera");
    UserDto result = userService.updateById(1L, userDto);

    assertThat(result.getName()).isEqualTo("Valera");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateById_WhenUserNotFound_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Assertions.assertThrows(UserNotFoundException.class,
        () -> userService.updateById(1L, userDto));
    verify(userRepository).findById(1L);
  }

  @Test
  void updateById_WhenEmailExists_ShouldThrowException() {
    User otherUser = new User();
    otherUser.setId(2L);
    otherUser.setEmail("test@mail.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(otherUser));
    Assertions.assertThrows(UserAlreadyExistsException.class,
        () -> userService.updateById(1L, userDto));
    verify(userRepository).findById(1L);
    verify(userRepository).findByEmail(userDto.getEmail());
  }

  @Test
  void getCardsByUserId_ShouldReturnActiveCards() {
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(user));
    when(cardRepository.findByUserIdAndActiveTrue(1L)).thenReturn(
        List.of(paymentCard, paymentCard2));

    List<PaymentCardDto> result = userService.getCardsByUserId(1L);

    assertThat(result).hasSize(2);

    verify(cardRepository).findByUserIdAndActiveTrue(1L);
  }

  @Test
  void getCardsByUserId_WhenNoCards_ShouldReturnEmptyList() {
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(user));
    when(cardRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of());

    List<PaymentCardDto> result = userService.getCardsByUserId(1L);

    assertThat(result).isEmpty();

    verify(cardRepository).findByUserIdAndActiveTrue(1L);
  }

  @Test
  void getCardsByUserId_WhenUserNotFound_ShouldThrowException() {
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

    var userId = user.getId();

    Assertions.assertThrows(UserNotFoundException.class,
        () -> userService.getCardsByUserId(userId));
  }

  @Test
  void setActiveTrue_WhenUserExists_ShouldReturnActivatedUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDto activatedUser = userService.setActive(1L, true);

    assertThat(activatedUser.getActive()).isTrue();

    verify(userRepository).findById(1L);
    verify(userRepository).save(user);
  }

  @Test
  void setActive_WhenUserNotFound_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Assertions.assertThrows(UserNotFoundException.class, () -> userService.setActive(1L, true));

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  void setActiveFalse_WhenUserExists_ShouldReturnDeactivatedUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDto activatedUser = userService.setActive(1L, false);

    assertThat(activatedUser.getActive()).isFalse();

    verify(userRepository).findById(1L);
    verify(userRepository).save(user);
  }

  @Test
  void delete_WhenUserExists_ShouldReturnVoid() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.delete(1L);

    verify(userRepository).findById(1L);
    verify(userRepository).deleteById(1L);
  }

  @Test
  void delete_WhenUserNotFound_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Assertions.assertThrows(UserNotFoundException.class, () -> userService.delete(1L));

    verify(userRepository).findById(1L);
    verify(userRepository, never()).deleteById(any());
  }
}
