package org.innowise.userservice.controller.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.model.entity.PaymentCard;
import org.innowise.userservice.model.entity.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class PaymentCardDataFactory {

    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String USER_ID = "userId";
    public static final String NUMBER = "number";
    public static final String HOLDER = "holder";
    public static final String EXPIRATION_DATE = "expirationDate";

    private final PaymentCardRepository cardRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong cardCounter = new AtomicLong();

    public PaymentCardDataFactory(PaymentCardRepository paymentCardRepository) {
        this.cardRepository = paymentCardRepository;
    }

    public PaymentCard createRandomCard(User user) {
        long number = 1000 + cardCounter.incrementAndGet();
        String holder = user.getName();
        LocalDate exp = LocalDate.now().plusYears(3);
        return createCard(user, number, holder, exp);
    }

    public PaymentCard createCard(User user, long number, String holder, LocalDate expirationDate) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        return cardRepository.save(card);
    }

    public PaymentCardDto performCreateCard(MockMvc mockMvc, PaymentCardDto paymentCardDto) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCardDto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
    }

    public PaymentCardDto performGetCardById(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
    }

    public List<PaymentCardDto> performGetAllCards(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE)
                        .param(PAGE,"0").param(SIZE,"10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.readValue(root.get("content").toString(), new TypeReference<List<PaymentCardDto>>() {});
    }

    public List<PaymentCardDto> performGetAllCardsByUserId(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(ApiConstant.CARDS_BASE + ApiConstant.CARDS_BY_USER_ID, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<PaymentCardDto>>() {});
    }

    public PaymentCardDto performUpdateCard(MockMvc mockMvc, Long id, PaymentCardDto paymentCardDto) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.CARD_ID_PATH, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCardDto)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
    }

    public PaymentCardDto performActivateCard(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.ACTIVATE_CARD, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
    }

    public PaymentCardDto performDeactivateCard(MockMvc mockMvc, Long id) throws Exception {
        MvcResult result = mockMvc.perform(post(ApiConstant.CARDS_BASE + ApiConstant.DEACTIVATE_CARD, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PaymentCardDto.class);
    }

}
