package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCardDataFactory {

  private final PaymentCardRepository cardRepository;

  public PaymentCardDataFactory(PaymentCardRepository paymentCardRepository) {
    this.cardRepository = paymentCardRepository;
  }

  public PaymentCard createAndSaveNewTestCard(User user) {
    String number = "1234567891234567";
    String holder = user.getName();
    LocalDate exp = LocalDate.now().plusYears(3);
    return saveTestCard(user, number, holder, exp);
  }

  private PaymentCard saveTestCard(User user, String number, String holder,
      LocalDate expirationDate) {
    PaymentCard card = new PaymentCard();
    card.setUser(user);
    card.setNumber(number);
    card.setHolder(holder);
    card.setExpirationDate(expirationDate);
    return cardRepository.save(card);
  }
}
