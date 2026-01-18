package org.innowise.userservice.service.impl;

import org.innowise.userservice.exception.MaxCardsExceededException;
import org.innowise.userservice.exception.UserNotFoundException;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.exception.PaymentCardNotFoundException;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.entity.PaymentCard;
import org.innowise.userservice.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@Tag("unit")
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private PaymentCardMapper paymentCardMapper = Mappers.getMapper(PaymentCardMapper.class);

    @InjectMocks
    private PaymentCardServiceImpl paymentCardServiceImpl;

    private PaymentCard paymentCard;

    private PaymentCard paymentCard2;
    private PaymentCardDto paymentCardDto;

    private Page<PaymentCard> cardPage;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("TestName");
        user.setSurname("surname");
        user.setBirthDate(LocalDate.parse("2003-09-20"));
        user.setEmail("test@mail.com");

        paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setHolder("Andrei");
        paymentCard.setNumber(12L);
        paymentCard.setExpirationDate(LocalDate.of(2003, 9, 20));
        paymentCard.setUser(user);

        paymentCard2 = new PaymentCard();
        paymentCard2.setId(1L);
        paymentCard2.setHolder("Valera");
        paymentCard2.setNumber(12L);
        paymentCard2.setExpirationDate(LocalDate.of(2003, 9, 20));
        paymentCard2.setUser(user);

        paymentCardDto = new PaymentCardDto();
        paymentCardDto.setId(1L);
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setHolder("Andrei");
        paymentCardDto.setNumber(12L);
        paymentCardDto.setExpirationDate("2003-09-20");

        List<PaymentCard> paymentCardDtoList = List.of(paymentCard);

        cardPage = new PageImpl<>(paymentCardDtoList, PageRequest.of(0, 10), paymentCardDtoList.size());
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldSaveAndReturnDto() {
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(paymentCard);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(any(Long.class))).thenReturn(1L);

        PaymentCardDto result = paymentCardServiceImpl.create(paymentCardDto);

        Assertions.assertEquals(result.getNumber(), paymentCardDto.getNumber());
        verify(paymentCardRepository).save(any(PaymentCard.class));
        verify(userRepository).findById(any(Long.class));
        verify(paymentCardRepository).countByUserIdAndActiveTrue(any(Long.class));
    }

    @Test
    void create_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class,
                () -> paymentCardServiceImpl.create(paymentCardDto));

    }

    @Test
    void create_WhenMaxCardsExceeded_ShouldThrowException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserIdAndActiveTrue(any(Long.class))).thenReturn(6L);

        Assertions.assertThrows(MaxCardsExceededException.class,
                () -> paymentCardServiceImpl.create(paymentCardDto));

    }

    @Test
    void getById_WhenCardExists_ShouldReturnUser() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));

        PaymentCardDto result = paymentCardServiceImpl.getById(1L);

        Assertions.assertEquals(result.getNumber(), paymentCardDto.getNumber());
        verify(paymentCardRepository).findById(1L);
    }

    @Test
    void getById_WhenPaymentCardNotFound_ShouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentCardServiceImpl.getById(1L))
                .isInstanceOf(PaymentCardNotFoundException.class);

        verify(paymentCardRepository).findById(1L);
    }


    @Test
    void getAll_ShouldReturnPageOfPaymentCards() {
        when(paymentCardRepository.findAll(any(Pageable.class))).thenReturn(cardPage);

        Page<PaymentCardDto> result = paymentCardServiceImpl.getAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumber())
                .isEqualTo(12L);

        verify(paymentCardRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAll_WhenNoCards_ShouldReturnEmptyPage() {
        when(paymentCardRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PaymentCardDto> result = paymentCardServiceImpl.getAll(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(paymentCardRepository).findAll(any(Pageable.class));
    }



    @Test
    void getAllByUserId_ShouldReturnActiveCards() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(user));
        when(paymentCardRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(paymentCard, paymentCard2));

        List<PaymentCardDto> result = paymentCardServiceImpl.getAllByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getHolder()).isEqualTo("Valera");

        verify(paymentCardRepository).findByUserIdAndActiveTrue(1L);
    }

    @Test
    void getAllByUserId_WhenNoCards_ShouldReturnEmptyList() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(user));
        when(paymentCardRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of());

        List<PaymentCardDto> result = paymentCardServiceImpl.getAllByUserId(1L);

        assertThat(result).hasSize(0);

        verify(paymentCardRepository).findByUserIdAndActiveTrue(1L);
    }

    @Test
    void getAllByUserId_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class,
                () -> paymentCardServiceImpl.getAllByUserId(user.getId()));
    }

    @Test
    void activate_ShouldReturnActivatedPaymentCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCardDto activatedUser = paymentCardServiceImpl.activate(1L);

        assertThat(activatedUser.getActive()).isEqualTo(true);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void activate_WhenPaymentCardNotFound_ShouldThrowException() {
        when(paymentCardRepository.findById(1L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(PaymentCardNotFoundException.class,
                () -> paymentCardServiceImpl.activate(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void deactivate_ShouldReturnDeactivatedPaymentCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCardDto deactivatedUser = paymentCardServiceImpl.deactivate(1L);

        assertThat(deactivatedUser.getActive()).isEqualTo(false);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void deactivate_WhenPaymentCardNotFound_ShouldThrowException() {
        when(paymentCardRepository.findById(1L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(PaymentCardNotFoundException.class,
                () -> paymentCardServiceImpl.deactivate(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository, never()).save(any());
    }

}