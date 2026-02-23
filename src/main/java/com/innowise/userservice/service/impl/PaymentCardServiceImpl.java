package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.MaxCardsExceededException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "cards")
@Transactional
public class PaymentCardServiceImpl implements PaymentCardService {

  private static final int USER_MAX_CARDS = 5;

  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;
  private final PaymentCardMapper paymentCardMapper;

  @Override
  @CachePut(value = "cards", key = "#result.id")
  public PaymentCardDto create(PaymentCardDto paymentCardDto) {
    Long userId = paymentCardDto.getUserId();
    userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    if (USER_MAX_CARDS <= paymentCardRepository.countByUserIdAndActiveTrue(userId)) {
      throw new MaxCardsExceededException(userId);
    }
    return toDto(paymentCardRepository.save(toEntity(paymentCardDto)));
  }

  @Override
  @Cacheable(value = "cards", key = "#id")
  @Transactional(readOnly = true)
  public PaymentCardDto getById(Long id) {
    PaymentCard paymentCard = paymentCardRepository.findById(id)
        .orElseThrow(() -> new PaymentCardNotFoundException(id));
    return toDto(paymentCard);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentCardDto> getAll(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentCard> cards = paymentCardRepository.findAll(pageable);
    return cards.map(this::toDto);
  }

  @Override
  @Transactional
  @CachePut(value = "cards", key = "#result.id")
  public PaymentCardDto updateById(Long id, PaymentCardDto newPaymentCardDto) {
    PaymentCard updatedCard = paymentCardRepository.findById(id)
        .orElseThrow(() -> new PaymentCardNotFoundException(id));

    User user = userRepository.findById(newPaymentCardDto.getUserId())
        .orElseThrow(() -> new UserNotFoundException(newPaymentCardDto.getUserId()));

    updatedCard.setUser(user);
    updatedCard.setNumber(newPaymentCardDto.getNumber());
    updatedCard.setHolder(newPaymentCardDto.getHolder());
    updatedCard.setExpirationDate(newPaymentCardDto.getExpirationDate());
    updatedCard.setActive(true);
    return toDto(paymentCardRepository.save(updatedCard));
  }

  @Override
  @Transactional
  @CachePut(value = "cards", key = "#result.id")
  public PaymentCardDto setActive(Long id, boolean active) {
    PaymentCard activatedCard = paymentCardRepository.findById(id)
        .orElseThrow(() -> new PaymentCardNotFoundException(id));

    activatedCard.setActive(active);
    return toDto(paymentCardRepository.save(activatedCard));
  }

  @Override
  @CacheEvict(value = "cards", key = "#id")
  public void delete(Long id) {
    paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
    paymentCardRepository.deleteById(id);
  }

  private PaymentCardDto toDto(PaymentCard paymentCard) {
    return paymentCardMapper.toDto(paymentCard);
  }

  private PaymentCard toEntity(PaymentCardDto paymentCardDto) {
    return paymentCardMapper.toEntity(paymentCardDto);
  }
}
