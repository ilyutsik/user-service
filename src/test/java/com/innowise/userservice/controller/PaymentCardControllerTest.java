package com.innowise.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.IntegrationTestBase;
import com.innowise.userservice.controller.factory.PaymentCardDataFactory;
import com.innowise.userservice.controller.factory.UserDataFactory;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardActivePatchDto;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentCardControllerTest extends IntegrationTestBase {

  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private User testUser;
  private PaymentCard testCard;
  private PaymentCardDto testCardDto;
  private PaymentCardActivePatchDto activePatchDto;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private PaymentCardRepository cardRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PaymentCardDataFactory cardFactory;
  @Autowired
  private UserDataFactory userFactory;
  @Autowired
  private PaymentCardMapper cardMapper;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setName("testName");
    testUser.setSurname("testSurname");
    testUser.setEmail("test@gmail.com");
    testUser.setBirthDate(LocalDate.now());
    testUser = userRepository.save(testUser);

    testCard = new PaymentCard();
    testCard.setHolder("Andrei");
    testCard.setNumber("1234567891234567");
    testCard.setExpirationDate(LocalDate.now());
    testCard.setUser(testUser);
    testCard = cardRepository.save(testCard);

    testCardDto = new PaymentCardDto();
    testCardDto.setHolder("Andrei");
    testCardDto.setNumber("1234567891234567");
    testCardDto.setExpirationDate(LocalDate.now());
    testCardDto.setUserId(testUser.getId());

    activePatchDto = new PaymentCardActivePatchDto();
    activePatchDto.setActive(true);
  }

  @AfterEach
  void clearDb() {
    cardRepository.deleteAll();
    userRepository.deleteAll();
  }

  private void assertCache(PaymentCardDto paymentCardDto) {
    Cache cache = cacheManager.getCache("cards");
    Object cached = cache.get(paymentCardDto.getId()).get();

    PaymentCardDto cachedDto = objectMapper.convertValue(cached, PaymentCardDto.class);

    Assertions.assertThat(cachedDto).isEqualTo(paymentCardDto);
  }

  @Test
  void createCard_whenValidCard_shouldReturnCreated() throws Exception {
    MvcResult result = mockMvc.perform(post(CardApi.BASE)
            .with(user("admin").roles("ADMIN"))
            .header("X-USER-ID", testCardDto.getUserId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isCreated())
        .andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(fetched.getHolder()).isEqualTo(testCardDto.getHolder());
    assertCache(fetched);
  }

  @Test
  void createCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
    testCardDto.setUserId(99L);
    mockMvc.perform(post(CardApi.BASE)
        .with(user("admin").roles("ADMIN"))
            .header("X-USER-ID", testCardDto.getUserId())
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createCard_whenUserAlreadyHas5Cards_shouldReturnBadRequest() throws Exception {
    for (int i = 2; i <= 5; i++) {
      cardFactory.createAndSaveNewTestCard(testUser);
    }
    mockMvc.perform(post(CardApi.BASE)
        .with(user("admin").roles("ADMIN"))
        .header("X-USER-ID", testCardDto.getUserId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getCardById_whenCardExist_shouldReturnCard() throws Exception {
    MvcResult result = mockMvc.perform(
            get(CardApi.BASE + CardApi.ID, testCard.getId())
                .with(user("admin").roles("ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(fetched.getHolder()).isEqualTo(testCard.getHolder());
    assertCache(fetched);
  }

  @Test
  void getCardById_whenCardDoesNotExist_shouldReturnNotFund() throws Exception {
    mockMvc.perform(get(CardApi.BASE + CardApi.ID, 99L)
            .with(user("admin").roles("ADMIN"))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAllCards_shouldReturnCards() throws Exception {
    MvcResult result = mockMvc.perform(
            get(CardApi.BASE).param(PAGE, "0").param(SIZE, "10")
                .with(user("admin").roles("ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<PaymentCardDto> cards = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<PaymentCardDto>>() {
        });
    Assertions.assertThat(cards).hasSize(1);
  }

  @Test
  void updateCard_whenValid_shouldReturnOk() throws Exception {
    MvcResult result = mockMvc.perform(
            put(CardApi.BASE + CardApi.ID, testCard.getId())
                .with(user("admin").roles("ADMIN"))
                .header("X-USER-ID", testCardDto.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isOk())
        .andReturn();
    PaymentCardDto updated = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(updated.getHolder()).isEqualTo(testCardDto.getHolder());
    assertCache(updated);
  }

  @Test
  void updateCard_whenCardDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(put(CardApi.BASE + CardApi.ID, 99L)
        .with(user("admin").roles("ADMIN"))
        .header("X-USER-ID", testCardDto.getUserId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isNotFound());
  }

  @Test
  void updateCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
    testCardDto.setUserId(99L);
    mockMvc.perform(
            put(CardApi.BASE + CardApi.ID, testCard.getId())
                .with(user("admin").roles("ADMIN"))
                .header("X-USER-ID", testCardDto.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void activateCard_whenExist_shouldReturnOk() throws Exception {
    MvcResult result = mockMvc.perform(
        patch(CardApi.BASE + CardApi.ID, testCard.getId())
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(activePatchDto))
            .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    assertThat(fetched.getActive()).isTrue();
  }

  @Test
  void activateCard_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(patch(CardApi.BASE + CardApi.ID, 99L)
        .with(user("admin").roles("ADMIN"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(activePatchDto))
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
  }

  @Test
  void deactivateCard_whenExist_shouldReturnOk() throws Exception {
    activePatchDto.setActive(false);
    MvcResult result = mockMvc.perform(
        patch(CardApi.BASE + CardApi.ID, testCard.getId())
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(activePatchDto))
            .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    assertThat(fetched.getActive()).isFalse();
  }

  @Test
  void deleteCard_whenExists_shouldReturnNoContent() throws Exception {
    mockMvc.perform(
            delete(CardApi.BASE + CardApi.ID, testCard.getId())
                .with(user("admin").roles("ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteCard_whenDoesNotExists_shouldReturnNotFound() throws Exception {
    mockMvc.perform(delete(CardApi.BASE + CardApi.ID, 99L)
            .with(user("admin").roles("ADMIN"))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
