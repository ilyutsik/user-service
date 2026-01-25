package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.PaymentCardDto;
import org.springframework.data.domain.Page;

public interface PaymentCardService {

  PaymentCardDto create(PaymentCardDto paymentCardDto);

  PaymentCardDto getById(Long id);

  Page<PaymentCardDto> getAll(int page, int size);

  PaymentCardDto updateById(Long id, PaymentCardDto newPaymentCardDto);

  PaymentCardDto setActive(Long id, boolean active);

  void delete(Long id);
}
