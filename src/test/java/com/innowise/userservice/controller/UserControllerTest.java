package com.innowise.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.IntegrationTestBase;
import com.innowise.userservice.constant.UserApi;
import com.innowise.userservice.controller.factory.UserDataFactory;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerTest extends IntegrationTestBase {

  public static final String NAME = "name";
  public static final String SURNAME = "surname";

  public static final String PAGE = "page";
  public static final String SIZE = "size";
  User testUser;
  UserDto testUserDto;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserDataFactory factory;
  @Autowired
  private UserMapper userMapper;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setName("Vova");
    testUser.setSurname("Popov");
    testUser.setEmail("test@mail.com");
    testUser.setBirthDate(LocalDate.now());
    userRepository.save(testUser);

    testUserDto = new UserDto();
    testUserDto.setName("Andrei");
    testUserDto.setSurname("Ilyutsik");
    testUserDto.setEmail("test228@mail.com");
    testUserDto.setBirthDate("2000-01-01");
  }

  @AfterEach
  void clearDb() {
    userRepository.deleteAll();
  }

  private void assertCache(UserDto userDto) {
    Cache cache = cacheManager.getCache("users");
    assertThat(cache.get(userDto.getId(), UserDto.class)).isEqualTo(userDto);
  }

  @Test
  void create_whenValidUser_shouldReturnCreated() throws Exception {
    MvcResult result = mockMvc.perform(post(UserApi.BASE).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUserDto))).andExpect(status().isCreated())
        .andReturn();
    UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        UserDto.class);
    assertThat(fetched.getName()).isEqualTo(testUserDto.getName());
    assertCache(fetched);
  }

  @Test
  void create_whenInvalidUser_shouldReturnBadRequest() throws Exception {
    UserDto incorrectDto = toDto(factory.createRandomUser());
    incorrectDto.setName("");
    mockMvc.perform(post(UserApi.BASE).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(incorrectDto))).andExpect(status().isBadRequest());
  }

  @Test
  void userById_whenExist_shouldReturnUser() throws Exception {
    UserDto userDto = toDto(factory.createRandomUser());
    MvcResult result = mockMvc.perform(
            get(UserApi.BASE + UserApi.ID, userDto.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        UserDto.class);
    assertThat(fetched.getName()).isEqualTo(userDto.getName());
    assertCache(fetched);
  }

  @Test
  void userById_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get(UserApi.BASE + UserApi.ID, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void allUsers_ShouldReturnAllUsers() throws Exception {
    factory.createRandomUser();
    factory.createRandomUser();
    MvcResult result = mockMvc.perform(
        get(UserApi.BASE).param(PAGE, "0").param(SIZE, "10").param(NAME, "").param(SURNAME, "")
            .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<UserDto> users = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<UserDto>>() {
        });
    assertThat(users).hasSize(3);
  }

  @Test
  void allUsers_withNameFilter_shouldReturnFilteredUsers() throws Exception {
    User user = factory.createRandomUser();
    factory.createRandomUser();
    MvcResult result = mockMvc.perform(
            get(UserApi.BASE).param(PAGE, "0").param(SIZE, "10").param(NAME, user.getName())
                .param(SURNAME, "").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<UserDto> users = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<UserDto>>() {
        });
    assertThat(users).hasSize(1);
    assertThat(users.getFirst().getName()).isEqualTo(user.getName());
  }

  @Test
  void allUsers_withSurnameFilter_shouldReturnFilteredUsers() throws Exception {
    User user = factory.createRandomUser();
    factory.createRandomUser();
    MvcResult result = mockMvc.perform(
            get(UserApi.BASE).param(PAGE, "0").param(SIZE, "10").param(NAME, "")
                .param(SURNAME, user.getSurname()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<UserDto> users = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<UserDto>>() {
        });
    assertThat(users).hasSize(1);
    assertThat(users.getFirst().getSurname()).isEqualTo(user.getSurname());
  }

  @Test
  void allUsers_withBothFilters_shouldReturnFilteredUsers() throws Exception {
    User user1 = factory.createRandomUser();
    User user2 = factory.createRandomUser();
    MvcResult result = mockMvc.perform(
            get(UserApi.BASE).param(PAGE, "0").param(SIZE, "10").param(NAME, user1.getName())
                .param(SURNAME, user2.getSurname()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<UserDto> users = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<UserDto>>() {
        });
    assertThat(users).isEmpty();
  }

  @Test
  void update_whenValid_shouldReturnOk() throws Exception {
    UserDto savedUser = toDto(factory.createRandomUser());
    MvcResult result = mockMvc.perform(
            post(UserApi.BASE + UserApi.ID, savedUser.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto))).andExpect(status().isOk())
        .andReturn();
    UserDto updated = objectMapper.readValue(result.getResponse().getContentAsString(),
        UserDto.class);
    assertThat(updated.getName()).isEqualTo(testUserDto.getName());
    assertCache(updated);
  }

  @Test
  void activateUser_whenExist_shouldReturnOk() throws Exception {
    User user = factory.createRandomUser();
    MvcResult result = mockMvc.perform(
            post(UserApi.BASE + UserApi.ACTIVATE, user.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        UserDto.class);
    assertThat(fetched.getActive()).isTrue();
  }

  @Test
  void activateUser_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(post(UserApi.BASE + UserApi.ACTIVATE, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void deactivateUser_whenExist_shouldReturnOk() throws Exception {
    User user = factory.createRandomUser();
    MvcResult result = mockMvc.perform(
            post(UserApi.BASE + UserApi.DEACTIVATE, user.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        UserDto.class);
    assertThat(fetched.getActive()).isFalse();
  }

  @Test
  void deactivateUser_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(post(UserApi.BASE + UserApi.DEACTIVATE, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  public UserDto toDto(User user) {
    return userMapper.toDto(user);
  }
}
