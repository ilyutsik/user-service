package org.innowise.userservice.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.innowise.userservice.IntegrationTestBase;
import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.controller.factory.UserDataFactory;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.entity.User;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerTest extends IntegrationTestBase {

    public static final String NAME = "name";
    public static final String SURNAME = "surname";

    public static final String PAGE = "page";
    public static final String SIZE = "size";


    @Autowired private MockMvc mockMvc;

    @Autowired private CacheManager cacheManager;

    @Autowired private UserRepository userRepository;

    @Autowired private UserDataFactory factory;

    @Autowired private UserMapper userMapper;

    @Autowired private ObjectMapper objectMapper;

    User testUser;
    UserDto testUserDto;


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
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isCreated())
                .andReturn();
        UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(fetched.getName()).isEqualTo(testUserDto.getName());
        assertCache(fetched);
    }

    @Test
    void create_whenInvalidUser_shouldReturnBadRequest() throws Exception {
        UserDto incorrectDto = toDto(factory.createRandomUser());
        incorrectDto.setName("");
        mockMvc.perform(post(ApiConstant.USERS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incorrectDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userById_whenExist_shouldReturnUser() throws Exception {
        UserDto userDto = toDto(factory.createRandomUser());
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE + ApiConstant.USER_ID_PATH, userDto.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(fetched.getName()).isEqualTo(userDto.getName());
        assertCache(fetched);
    }

    @Test
    void userById_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(ApiConstant.USERS_BASE + ApiConstant.USER_ID_PATH, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void allUsers_ShouldReturnAllUsers() throws Exception {
        factory.createRandomUser();
        factory.createRandomUser();
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .param(NAME,"").param(SURNAME,"")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<UserDto> users = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<UserDto>>() {});
        assertThat(users.size()).isEqualTo(3);
    }

    @Test
    void allUsers_withNameFilter_shouldReturnFilteredUsers() throws Exception {
        User user = factory.createRandomUser();
        factory.createRandomUser();
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .param(NAME,user.getName()).param(SURNAME,"")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<UserDto> users = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<UserDto>>() {});
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getName()).isEqualTo(user.getName());
    }

    @Test
    void allUsers_withSurnameFilter_shouldReturnFilteredUsers() throws Exception {
        User user = factory.createRandomUser();
        factory.createRandomUser();
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .param(NAME, "").param(SURNAME,user.getSurname())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<UserDto> users = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<UserDto>>() {});
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getSurname()).isEqualTo(user.getSurname());
    }

    @Test
    void allUsers_withBothFilters_shouldReturnFilteredUsers() throws Exception {
        User user1 = factory.createRandomUser();
        User user2 = factory.createRandomUser();
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .param(NAME, user1.getName()).param(SURNAME,user2.getSurname())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<UserDto> users = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<UserDto>>() {});
        assertThat(users.size()).isEqualTo(0);
    }

    @Test
    void update_whenValid_shouldReturnOk() throws Exception {
        UserDto savedUser = toDto(factory.createRandomUser());
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.USER_ID_PATH, savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isOk())
                .andReturn();
        UserDto updated = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(updated.getName()).isEqualTo(testUserDto.getName());
        assertCache(updated);
    }

    @Test
    void activateUser_whenExist_shouldReturnOk() throws Exception {
        User user = factory.createRandomUser();
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.ACTIVATE_USER, user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(fetched.getActive()).isEqualTo(true);
    }

    @Test
    void activateUser_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.ACTIVATE_USER, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateUser_whenExist_shouldReturnOk() throws Exception {
        User user = factory.createRandomUser();
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.DEACTIVATE_USER, user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(fetched.getActive()).isEqualTo(false);
    }

    @Test
    void deactivateUser_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.DEACTIVATE_USER, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    public UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

}
