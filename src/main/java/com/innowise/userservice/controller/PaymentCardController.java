package com.innowise.userservice.controller;

import com.innowise.userservice.constant.CardApi;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CardApi.BASE)
@RequiredArgsConstructor
public class PaymentCardController {

  private final PaymentCardServiceImpl paymentCardServiceImpl;

  @PostMapping()
  public ResponseEntity<PaymentCardDto> create(@Valid @RequestBody PaymentCardDto paymentCardDto) {
    PaymentCardDto createdDto = paymentCardServiceImpl.create(paymentCardDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
  }

  @GetMapping(CardApi.ID)
  public ResponseEntity<PaymentCardDto> getById(@PathVariable(name = "id") Long id) {
    PaymentCardDto paymentCardDto = paymentCardServiceImpl.getById(id);
    return ResponseEntity.ok(paymentCardDto);
  }

  @GetMapping
  public ResponseEntity<Page<PaymentCardDto>> getAll(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    Page<PaymentCardDto> cards = paymentCardServiceImpl.getAll(page, size);
    return ResponseEntity.ok(cards);
  }

  @GetMapping(CardApi.BY_USER_ID)
  public ResponseEntity<List<PaymentCardDto>> getAllByUserId(
      @PathVariable(name = "userId") Long userId) {
    List<PaymentCardDto> cards = paymentCardServiceImpl.getAllByUserId(userId);
    return ResponseEntity.ok(cards);
  }

  @PostMapping(CardApi.ID)
  public ResponseEntity<PaymentCardDto> update(@PathVariable(name = "id") Long id,
      @Valid @RequestBody PaymentCardDto paymentCardDto) {

    PaymentCardDto updatedCardDto = paymentCardServiceImpl.updateById(id, paymentCardDto);
    return ResponseEntity.ok(updatedCardDto);
  }

  @PatchMapping(CardApi.ACTIVATE)
  public ResponseEntity<PaymentCardDto> activate(@PathVariable(name = "id") Long id) {
    PaymentCardDto activatedCard = paymentCardServiceImpl.activate(id);
    return ResponseEntity.ok(activatedCard);
  }

  @PatchMapping(CardApi.DEACTIVATE)
  public ResponseEntity<PaymentCardDto> deactivate(@PathVariable(name = "id") Long id) {
    PaymentCardDto deactivatedCard = paymentCardServiceImpl.deactivate(id);
    return ResponseEntity.ok(deactivatedCard);
  }

  @DeleteMapping(CardApi.ID)
  public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
    paymentCardServiceImpl.delete(id);
    return ResponseEntity.noContent().build();
  }
}
