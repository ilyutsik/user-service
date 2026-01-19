package com.innowise.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.IntegrationTestBase;
import com.innowise.userservice.constant.CardApi;
import com.innowise.userservice.controller.factory.PaymentCardDataFactory;
import com.innowise.userservice.controller.factory.UserDataFactory;
import com.innowise.userservice.mapper.PaymentCardMapper;
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
public class PaymentCardControllerTest extends IntegrationTestBase {

  public static final String PAGE = "page";
  public static final String SIZE = "size";
  User testUser;
  PaymentCard testCard;
  PaymentCardDto testCardDto;
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
    testCard.setNumber(12L);
    testCard.setExpirationDate(LocalDate.now());
    testCard.setUser(testUser);
    testCard = cardRepository.save(testCard);

    testCardDto = new PaymentCardDto();
    testCardDto.setHolder("Andrei");
    testCardDto.setNumber(12L);
    testCardDto.setExpirationDate(LocalDate.now().toString());
    testCardDto.setUserId(testUser.getId());
  }

  @AfterEach
  void clearDb() {
    cardRepository.deleteAll();
    userRepository.deleteAll();
  }

  private void assertCache(PaymentCardDto paymentCardDto) {
    Cache cache = cacheManager.getCache("cards");
    Assertions.assertThat(cache.get(paymentCardDto.getId(), PaymentCardDto.class))
        .isEqualTo(paymentCardDto);
  }

  @Test
  void createCard_whenValidCard_shouldReturnCreated() throws Exception {
    MvcResult result = mockMvc.perform(post(CardApi.BASE).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isCreated())
        .andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(fetched.getHolder()).isEqualTo(testCardDto.getHolder());
    assertCache(fetched);
  }

  @Test
  void createCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
    testCardDto.setUserId(99L);
    mockMvc.perform(post(CardApi.BASE).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isNotFound());
  }

  @Test
  void createCard_whenUserAlreadyHas5Cards_shouldReturnBadRequest() throws Exception {
    for (int i = 2; i <= 5; i++) {
      cardFactory.createAndSaveNewTestCard(testUser);
    }
    mockMvc.perform(post(CardApi.BASE).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isBadRequest());
  }

  @Test
  void getCardById_whenCardExist_shouldReturnCard() throws Exception {
    MvcResult result = mockMvc.perform(
            get(CardApi.BASE + CardApi.ID, testCard.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(fetched.getHolder()).isEqualTo(testCard.getHolder());
    assertCache(fetched);
  }

  @Test
  void getCardById_whenCardDoesNotExist_shouldReturnNotFund() throws Exception {
    mockMvc.perform(get(CardApi.BASE + CardApi.ID, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAllCards_shouldReturnCards() throws Exception {
    MvcResult result = mockMvc.perform(
            get(CardApi.BASE).param(PAGE, "0").param(SIZE, "10").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    List<PaymentCardDto> cards = objectMapper.readValue(root.get("content").toString(),
        new TypeReference<List<PaymentCardDto>>() {
        });
    Assertions.assertThat(cards).hasSize(1);
  }

  @Test
  void getCardsByUserId_whenExist_shouldReturnCards() throws Exception {
    cardFactory.createAndSaveNewTestCard(testUser);
    MvcResult result = mockMvc.perform(
            get(CardApi.BASE + CardApi.BY_USER_ID, testUser.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    List<PaymentCardDto> cards = objectMapper.readValue(result.getResponse().getContentAsString(),
        new TypeReference<List<PaymentCardDto>>() {
        });
    Assertions.assertThat(cards).hasSize(2);
  }

  @Test
  void getCardsByUserId_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get(CardApi.BASE + CardApi.BY_USER_ID, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateCard_whenValid_shouldReturnOk() throws Exception {
    MvcResult result = mockMvc.perform(
            post(CardApi.BASE + CardApi.ID, testCard.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isOk())
        .andReturn();
    PaymentCardDto updated = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    Assertions.assertThat(updated.getHolder()).isEqualTo(testCardDto.getHolder());
    assertCache(updated);
  }

  @Test
  void updateCard_whenCardDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(post(CardApi.BASE + CardApi.ID, 99L).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testCardDto))).andExpect(status().isNotFound());
  }

  @Test
  void updateCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
    testCardDto.setUserId(99L);
    mockMvc.perform(
            post(CardApi.BASE + CardApi.ID, testCard.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCardDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void activateCard_whenExist_shouldReturnOk() throws Exception {
    MvcResult result = mockMvc.perform(
            patch(CardApi.BASE + CardApi.ACTIVATE, testCard.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    assertThat(fetched.getActive()).isTrue();
  }

  @Test
  void activateCard_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(patch(CardApi.BASE + CardApi.ACTIVATE, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void deactivateCard_whenExist_shouldReturnOk() throws Exception {
    MvcResult result = mockMvc.perform(
        patch(CardApi.BASE + CardApi.DEACTIVATE, testCard.getId()).accept(
            MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(),
        PaymentCardDto.class);
    assertThat(fetched.getActive()).isFalse();
  }

  @Test
  void deactivateCard_whenDoesNotExist_shouldReturnNotFound() throws Exception {
    mockMvc.perform(
            patch(CardApi.BASE + CardApi.DEACTIVATE, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteCard_whenExists_shouldReturnNoContent() throws Exception {
    mockMvc.perform(
            delete(CardApi.BASE + CardApi.ID, testCard.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteCard_whenDoesNotExists_shouldReturnNotFound() throws Exception {
    mockMvc.perform(delete(CardApi.BASE + CardApi.ID, 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
