package org.innowise.userservice.service.impl;

import org.innowise.userservice.exception.UserAlreadyExistsException;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.exception.UserNotFoundException;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @Mock private PaymentCardServiceImpl paymentCardServiceImpl;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User user;
    private UserDto userDto;

    private Page<User> userPage;

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
        userDto.setBirthDate("2000-01-01");
        userDto.setActive(true);

        List<User> userList = List.of(user);
        userPage = new PageImpl<>(userList, PageRequest.of(0, 10), userList.size());
    }

    @Test
    void create_WhenUserDoesNotExists_ShouldSaveAndReturnDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userServiceImpl.create(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_WhenUserAlreadyExist_ShouldThrowException() {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userServiceImpl.create(userDto));
    }


    @Test
    void getById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userServiceImpl.getById(1L);

        assertThat(result.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void getById_UserNotFond_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userServiceImpl.getById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAll_WhenUsersExists_ShouldReturnUsers() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserDto> page = userServiceImpl.getAll(0, 10, "", "");

        assertThat(page.get().findFirst().get().getSurname()).isEqualTo("Ilyutsik");
    }

    @Test
    void updateById_WhenUserExists_ShouldUpdateFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        userDto.setName("Valera");
        UserDto result = userServiceImpl.updateById(1L, userDto);

        assertThat(result.getName()).isEqualTo("Valera");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userServiceImpl.updateById(1L, userDto));
        verify(userRepository).findById(1L);
    }

    @Test
    void updateById_WhenEmailExists_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.updateById(1L, userDto));
        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail(userDto.getEmail());
    }

    @Test
    void activate_WhenUserExists_ShouldReturnActivatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto activatedUser = userServiceImpl.activate(1L);

        assertThat(activatedUser.getActive()).isEqualTo(true);

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void activate_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userServiceImpl.activate(1L));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivate_WhenUserExists_ShouldReturnDeactivatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto activatedUser = userServiceImpl.deactivate(1L);

        assertThat(activatedUser.getActive()).isEqualTo(false);

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void deactivate_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userServiceImpl.deactivate(1L));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

}
