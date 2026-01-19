package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class PaymentCardDataFactory {

  private final PaymentCardRepository cardRepository;
  private final AtomicLong cardCounter = new AtomicLong();

  public PaymentCardDataFactory(PaymentCardRepository paymentCardRepository) {
    this.cardRepository = paymentCardRepository;
  }

  public PaymentCard createAndSaveNewTestCard(User user) {
    long number = 1000 + cardCounter.incrementAndGet();
    String holder = user.getName();
    LocalDate exp = LocalDate.now().plusYears(3);
    return saveTestCard(user, number, holder, exp);
  }

  private PaymentCard saveTestCard(User user, long number, String holder,
      LocalDate expirationDate) {
    PaymentCard card = new PaymentCard();
    card.setUser(user);
    card.setNumber(number);
    card.setHolder(holder);
    card.setExpirationDate(expirationDate);
    return cardRepository.save(card);
  }
}
