package org.innowise.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.exception.MaxCardsExceededException;
import org.innowise.userservice.exception.PaymentCardNotFoundException;
import org.innowise.userservice.exception.UserNotFoundException;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.entity.PaymentCard;
import org.innowise.userservice.model.entity.User;
import org.innowise.userservice.service.PaymentCardService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "cards")
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int USER_MAX_CARDS = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @CachePut(value = "cards", key = "#result.id")
    public PaymentCardDto create(PaymentCardDto paymentCardDto) {
        Long userId = paymentCardDto.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (USER_MAX_CARDS <= paymentCardRepository.countByUserIdAndActiveTrue(userId))
            throw new MaxCardsExceededException(userId);
        return toDto(paymentCardRepository.save(toEntity(paymentCardDto)));
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public PaymentCardDto getById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
        return toDto(paymentCard);
    }

    @Override
    public Page<PaymentCardDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentCard> cards = paymentCardRepository.findAll(pageable);
        return cards.map(this::toDto);
    }

    @Override
    public List<PaymentCardDto> getAllByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<PaymentCard> cardList = paymentCardRepository.findByUserIdAndActiveTrue(userId);
        return cardList.stream().map(this::toDto).toList();
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
        updatedCard.setExpirationDate(LocalDate.parse(newPaymentCardDto.getExpirationDate()));
        updatedCard.setActive(true);
        return toDto(paymentCardRepository.save(updatedCard));
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#result.id")
    public PaymentCardDto activate(Long id) {
        PaymentCard activatedCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        activatedCard.setActive(true);
        return toDto(paymentCardRepository.save(activatedCard));
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#result.id")
    public PaymentCardDto deactivate(Long id) {
        PaymentCard deactivatedCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        deactivatedCard.setActive(false);
        return toDto(paymentCardRepository.save(deactivatedCard));
    }

    private PaymentCardDto toDto(PaymentCard paymentCard) {
        return paymentCardMapper.toDto(paymentCard);
    }

    private PaymentCard toEntity(PaymentCardDto paymentCardDto) {
        return paymentCardMapper.toEntity(paymentCardDto);
    }

}
