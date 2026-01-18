package org.innowise.userservice.controller;

import org.assertj.core.api.Assertions;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.controller.factory.PaymentCardDataFactory;
import org.innowise.userservice.controller.factory.UserDataFactory;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.entity.PaymentCard;
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

import java.time.LocalDate;
import java.util.List;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class PaymentCardIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private CacheManager cacheManager;

    @Autowired private PaymentCardRepository cardRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private PaymentCardDataFactory cardFactory;
    @Autowired private UserDataFactory userFactory;

    @Autowired private PaymentCardMapper cardMapper;

    User testUser;
    PaymentCardDto testCardDto;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        cardRepository.deleteAll();
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("TestName");

        testCardDto = new PaymentCardDto();
        testCardDto.setHolder("Andrei");
        testCardDto.setNumber(12L);
        testCardDto.setExpirationDate(LocalDate.of(2003, 9, 20).toString());
        testCardDto.setUserId(testUser.getId());
    }

    public PaymentCardDto toDto(PaymentCard paymentCard) {
        return cardMapper.toDto(paymentCard);
    }

    private void assertCache(PaymentCardDto paymentCardDto) {
        Cache cache = cacheManager.getCache("cards");
        Assertions.assertThat(cache.get(paymentCardDto.getId(), PaymentCardDto.class)).isEqualTo(paymentCardDto);
    }

    @Test
    void shouldCreateCard() throws Exception {
        User user = userFactory.createRandomUser();
        testCardDto.setUserId(user.getId());
        PaymentCardDto fetched = cardFactory.performCreateCard(mockMvc, testCardDto);
        Assertions.assertThat(fetched.getHolder()).isEqualTo(testCardDto.getHolder());
        assertCache(fetched);
    }

    @Test
    void shouldReturnCardById() throws Exception {
        User user = userFactory.createRandomUser();
        PaymentCardDto paymentCardDto = toDto(cardFactory.createRandomCard(user));
        PaymentCardDto fetched = cardFactory.performGetCardById(mockMvc, paymentCardDto.getId());
        Assertions.assertThat(fetched.getHolder()).isEqualTo(paymentCardDto.getHolder());
        assertCache(fetched);
    }

    @Test
    void shouldReturnAllCards() throws Exception {
        User user = userFactory.createRandomUser();
        cardFactory.createRandomCard(user);
        cardFactory.createRandomCard(user);
        List<PaymentCardDto> cards = cardFactory.performGetAllCards(mockMvc);
        Assertions.assertThat(cards.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnAllCardsByUserId() throws Exception {
        User user = userFactory.createRandomUser();
        cardFactory.createRandomCard(user);
        cardFactory.createRandomCard(user);
        cardFactory.createRandomCard(user);
        List<PaymentCardDto> cards = cardFactory.performGetAllCardsByUserId(mockMvc, user.getId());
        Assertions.assertThat(cards.size()).isEqualTo(3);
    }

    @Test
    void shouldUpdateCardAndCache() throws Exception {
        User user = userFactory.createRandomUser();
        testCardDto.setUserId(user.getId());
        PaymentCard cardToUpdate = cardFactory.createRandomCard(user);
        PaymentCardDto updated = cardFactory.performUpdateCard(mockMvc, cardToUpdate.getId(), testCardDto);
        Assertions.assertThat(updated.getHolder()).isEqualTo(testCardDto.getHolder());
        assertCache(updated);
    }

    @Test
    void shouldActivateAndDeactivateUser() throws Exception {
        User user = userFactory.createRandomUser();

        PaymentCardDto c1 = toDto(cardFactory.createRandomCard(user));
        Assertions.assertThat(c1.getActive()).isEqualTo(true);

        PaymentCardDto c2 = cardFactory.performDeactivateCard(mockMvc, c1.getId());
        Assertions.assertThat(c2.getActive()).isEqualTo(false);

        PaymentCardDto c3 = cardFactory.performActivateCard(mockMvc, c2.getId());
        Assertions.assertThat(c3.getActive()).isEqualTo(true);

        assertCache(c3);
    }

}
