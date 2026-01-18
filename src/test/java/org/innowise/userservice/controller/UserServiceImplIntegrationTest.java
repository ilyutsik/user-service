package org.innowise.userservice.controller;


import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.controller.factory.UserDataFactory;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class UserServiceImplIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private CacheManager cacheManager;

    @Autowired private UserRepository userRepository;

    @Autowired private UserDataFactory factory;

    @Autowired private UserMapper userMapper;

    UserDto testUserDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUserDto = new UserDto();
        testUserDto.setName("Andrei");
        testUserDto.setSurname("Ilyutsik");
        testUserDto.setEmail("test@mail.com");
        testUserDto.setBirthDate("2000-01-01");
        testUserDto.setActive(true);
    }

    public UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    private void assertCache(UserDto userDto) {
        Cache cache = cacheManager.getCache("users");
        assertThat(cache.get(userDto.getId(), UserDto.class)).isEqualTo(userDto);
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserDto fetched = factory.performCreateUser(mockMvc, testUserDto);
        assertThat(fetched.getName()).isEqualTo(testUserDto.getName());
        assertCache(fetched);
    }

    @Test
    void shouldReturnUserById() throws Exception {
        UserDto userDto = toDto(factory.createRandomUser());
        UserDto fetched = factory.performGetUserById(mockMvc, userDto.getId());
        assertThat(fetched.getName()).isEqualTo(userDto.getName());
        assertCache(fetched);
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        factory.createRandomUser();
        factory.createRandomUser();
        List<UserDto> users = factory.performGetAllUsers(mockMvc);
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateUserAndCache() throws Exception {
        UserDto savedUser = toDto(factory.createRandomUser());
        UserDto updated = factory.performUpdateUser(mockMvc, savedUser.getId(), testUserDto);
        assertThat(updated.getName()).isEqualTo(testUserDto.getName());
        assertCache(updated);
    }

    @Test
    void shouldActivateAndDeactivateUser() throws Exception {
        UserDto u1 = toDto(factory.createRandomUser());
        assertThat(u1.getActive()).isEqualTo(true);
        UserDto u2 = factory.performDeactivateUser(mockMvc, u1.getId());
        assertThat(u2.getActive()).isEqualTo(false);
        UserDto u3 = factory.performActivateUser(mockMvc, u2.getId());
        assertThat(u3.getActive()).isEqualTo(true);
        assertCache(u3);
    }

}
