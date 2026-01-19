package com.innowise.userservice.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class PaymentCardDataFactory {

  public static final String PAGE = "page";
  public static final String SIZE = "size";
  public static final String USER_ID = "userId";
  public static final String NUMBER = "number";
  public static final String HOLDER = "holder";
  public static final String EXPIRATION_DATE = "expirationDate";
  private final PaymentCardRepository cardRepository;
  private final AtomicLong cardCounter = new AtomicLong();
  ObjectMapper objectMapper = new ObjectMapper();

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
}
