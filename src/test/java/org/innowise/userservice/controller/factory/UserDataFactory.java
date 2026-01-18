package org.innowise.userservice.controller.factory;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.model.entity.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class UserDataFactory {

    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String EMAIL = "email";
    public static final String BIRTH_DATE = "birthDate";
    public static final String PAGE = "page";
    public static final String SIZE = "size";

    private final UserRepository userRepository;
    private ObjectMapper objectMapper;

    private final AtomicLong userCounter = new AtomicLong();

    public UserDataFactory(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public User createRandomUser() {
        String name = "User" + userCounter.incrementAndGet();
        String surname = "Popov" + userCounter.get();
        String email = "user" + userCounter.get() + "@test.com";
        LocalDate birth = LocalDate.of(2000,1,1);
        return createUser(name, surname, email, birth);
    }

    public User createUser(String name, String surname, String email, LocalDate birthDate) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        return userRepository.save(user);
    }

    public UserDto performCreateUser(MockMvc mockMvc, UserDto userDto) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    public UserDto performGetUserById(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE + ApiConstant.USER_ID_PATH, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    public List<UserDto> performGetAllUsers(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.USERS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .param(NAME,"").param(SURNAME,"")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.readValue(root.get("content").toString(), new TypeReference<List<UserDto>>() {});
    }

    public UserDto performUpdateUser(MockMvc mockMvc, Long id, UserDto userDto) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.USER_ID_PATH, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    public UserDto performActivateUser(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.ACTIVATE_USER, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

    public UserDto performDeactivateUser(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.USERS_BASE + ApiConstant.DEACTIVATE_USER, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
    }

}
