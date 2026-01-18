package org.innowise.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.innowise.userservice.IntegrationTestBase;
import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.model.dto.UserDto;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.controller.factory.PaymentCardDataFactory;
import org.innowise.userservice.controller.factory.UserDataFactory;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.entity.PaymentCard;
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
public class PaymentCardControllerTest extends IntegrationTestBase {

    public static final String PAGE = "page";
    public static final String SIZE = "size";

    @Autowired private MockMvc mockMvc;

    @Autowired private CacheManager cacheManager;

    @Autowired private PaymentCardRepository cardRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private PaymentCardDataFactory cardFactory;

    @Autowired private UserDataFactory userFactory;

    @Autowired private PaymentCardMapper cardMapper;

    @Autowired private ObjectMapper objectMapper;

    User testUser;
    PaymentCard testCard;

    PaymentCardDto testCardDto;

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
        Assertions.assertThat(cache.get(paymentCardDto.getId(), PaymentCardDto.class)).isEqualTo(paymentCardDto);
    }

    @Test
    void createCard_whenValidCard_shouldReturnCreated() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isCreated())
                .andReturn();
        PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
        Assertions.assertThat(fetched.getHolder()).isEqualTo(testCardDto.getHolder());
        assertCache(fetched);
    }

    @Test
    void createCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        testCardDto.setUserId(99L);
        mockMvc.perform(post(ApiConstant.CARDS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_whenUserAlreadyHas5Cards_shouldReturnBadRequest() throws Exception {
        for (int i = 2; i <= 5; i++) {
            cardFactory.createRandomCard(testUser);
        }
        mockMvc.perform(post(ApiConstant.CARDS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_whenCardExist_shouldReturnCard() throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, testCard.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        PaymentCardDto fetched =  objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
        Assertions.assertThat(fetched.getHolder()).isEqualTo(testCard.getHolder());
        assertCache(fetched);
    }

    @Test
    void getCardById_whenCardDoesNotExist_shouldReturnNotFund() throws Exception {
        mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCards_shouldReturnCards() throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<PaymentCardDto> cards = objectMapper.readValue(root.get("content").toString(), new TypeReference<List<PaymentCardDto>>() {});
        Assertions.assertThat(cards.size()).isEqualTo(1);
    }

    @Test
    void getCardsByUserId_whenExist_shouldReturnCards() throws Exception {
        cardFactory.createRandomCard(testUser);
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARDS_BY_USER_ID, testUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<PaymentCardDto> cards = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<PaymentCardDto>>() {});
        Assertions.assertThat(cards.size()).isEqualTo(2);
    }

    @Test
    void getCardsByUserId_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARDS_BY_USER_ID, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_whenValid_shouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, testCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isOk())
                .andReturn();
        PaymentCardDto updated =  objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
        Assertions.assertThat(updated.getHolder()).isEqualTo(testCardDto.getHolder());
        assertCache(updated);
    }

    @Test
    void updateCard_whenCardDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        testCardDto.setUserId(99L);
        mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, testCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateCard_whenExist_shouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.ACTIVATE_CARD, testCard.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
        assertThat(fetched.getActive()).isEqualTo(true);
    }

    @Test
    void activateCard_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.ACTIVATE_CARD, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateCard_whenExist_shouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.DEACTIVATE_CARD, testCard.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        PaymentCardDto fetched = objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
        assertThat(fetched.getActive()).isEqualTo(false);
    }

    @Test
    void deactivateCard_whenDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.DEACTIVATE_CARD, 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
