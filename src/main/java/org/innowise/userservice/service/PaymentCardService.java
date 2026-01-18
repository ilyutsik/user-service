package org.innowise.userservice.service;

import org.innowise.userservice.model.dto.PaymentCardDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentCardService {

    PaymentCardDto create(PaymentCardDto paymentCardDto);

    PaymentCardDto getById(Long id);

    Page<PaymentCardDto> getAll(int page, int size);

    List<PaymentCardDto> getAllByUserId(Long userId);

    PaymentCardDto updateById(Long id, PaymentCardDto newPaymentCardDto);

    PaymentCardDto activate(Long id);

    PaymentCardDto deactivate(Long id);
}
