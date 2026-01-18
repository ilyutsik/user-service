package org.innowise.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.repository.UserSpecifications;
import org.innowise.userservice.exception.UserAlreadyExistsException;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.exception.UserNotFoundException;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.entity.User;
import org.innowise.userservice.service.UserService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @CachePut(value = "users", key = "#result.id")
    public UserDto create(UserDto userDto) {
        checkEmailUnique(userDto.getEmail());
        return toDto(userRepository.save(toEntity(userDto)));
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));
        return toDto(user);
    }

    @Override
    public Page<UserDto> getAll(int page, int size, String name, String surname) {
        Specification<User> spec = UserSpecifications.isActive()
                .and(UserSpecifications.hasName(name))
                .and(UserSpecifications.hasSurname(surname));

        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    @CachePut(key = "#result.id")
    public UserDto updateById(Long id, UserDto newUserDto) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        checkEmailUnique(newUserDto.getEmail());
        updatedUser.setName(newUserDto.getName());
        updatedUser.setSurname(newUserDto.getSurname());
        updatedUser.setEmail(newUserDto.getEmail());
        updatedUser.setBirthDate(LocalDate.parse(newUserDto.getBirthDate()));
        return toDto(userRepository.save(updatedUser));
    }

    @Override
    @Transactional
    @CachePut(key = "#result.id")
    public UserDto activate(Long id) {
        User activeUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        activeUser.setActive(true);
        return toDto(userRepository.save(activeUser));
    }

    @Override
    @Transactional
    @CachePut(key = "#result.id")
    public UserDto deactivate(Long id) {
        User deactivetedUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        deactivetedUser.setActive(false);
        return toDto(userRepository.save(deactivetedUser));
    }

    private void checkEmailUnique(String email) {
        userRepository.findByEmail(email).ifPresent(
                user -> {throw UserAlreadyExistsException.withEmail(email);
                });
    }

    private User toEntity(UserDto userDto) {
        return userMapper.toEntity(userDto);
    }
    private UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

}
