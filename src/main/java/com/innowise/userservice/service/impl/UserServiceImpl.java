package com.innowise.userservice.service.impl;

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
import com.innowise.userservice.repository.UserSpecifications;
import com.innowise.userservice.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PaymentCardRepository cardRepository;
  private final UserMapper userMapper;
  private final PaymentCardMapper cardMapper;

  @Override
  @CachePut(value = "users", key = "#result.id")
  public UserDto create(UserDto userDto) {
    checkEmailUnique(userDto.getEmail());
    return userToDto(userRepository.save(userDtoToEntity(userDto)));
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "users", key = "#id")
  public UserDto getById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    return userToDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    return userToDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserDto> getAll(int page, int size, String name, String surname) {
    Specification<User> spec = UserSpecifications.isActive().and(UserSpecifications.hasName(name))
        .and(UserSpecifications.hasSurname(surname));

    Pageable pageable = PageRequest.of(page, size);
    return userRepository.findAll(spec, pageable).map(this::userToDto);
  }

  @Override
  @CachePut(value = "users", key = "#result.id")
  public UserDto updateById(Long id, UserDto newUserDto) {
    User updatedUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    userRepository.findByEmail(newUserDto.getEmail()).ifPresent(user -> {
      if (!user.getId().equals(id)) {
        throw UserAlreadyExistsException.withEmail(newUserDto.getEmail());
      }
    });
    updatedUser.setName(newUserDto.getName());
    updatedUser.setSurname(newUserDto.getSurname());
    updatedUser.setEmail(newUserDto.getEmail());
    updatedUser.setBirthDate(newUserDto.getBirthDate());
    return userToDto(userRepository.save(updatedUser));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentCardDto> getCardsByUserId(Long userId) {
    userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    List<PaymentCard> cardList = cardRepository.findByUserIdAndActiveTrue(userId);
    return cardList.stream().map(this::cardToDto).toList();
  }

  @Override
  @CachePut(value = "users", key = "#result.id")
  public UserDto setActive(Long id, boolean active) {
    User activeUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    activeUser.setActive(active);
    return userToDto(userRepository.save(activeUser));
  }

  @Override
  @CacheEvict(value = "users", key = "#id")
  public void delete(Long id) {
    userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    userRepository.deleteById(id);
  }

  private void checkEmailUnique(String email) {
    userRepository.findByEmail(email).ifPresent(user -> {
      throw UserAlreadyExistsException.withEmail(email);
    });
  }

  private User userDtoToEntity(UserDto userDto) {
    return userMapper.toEntity(userDto);
  }

  private UserDto userToDto(User user) {
    return userMapper.toDto(user);
  }

  private PaymentCardDto cardToDto(PaymentCard paymentCard) {
    return cardMapper.toDto(paymentCard);
  }
}
