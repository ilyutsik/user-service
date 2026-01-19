package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class UserDataFactory {

  private final UserRepository userRepository;

  private final AtomicLong userCounter = new AtomicLong();

  public UserDataFactory(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createAndSaveNewTestUser() {
    String name = "User" + userCounter.incrementAndGet();
    String surname = "Popov" + userCounter.get();
    String email = "user" + userCounter.get() + "@test.com";
    LocalDate birth = LocalDate.of(2000, 1, 1);
    return saveTestUser(name, surname, email, birth);
  }

  private User saveTestUser(String name, String surname, String email, LocalDate birthDate) {
    User user = new User();
    user.setName(name);
    user.setSurname(surname);
    user.setEmail(email);
    user.setBirthDate(birthDate);
    return userRepository.save(user);
  }
}
