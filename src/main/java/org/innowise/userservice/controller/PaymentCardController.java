package org.innowise.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.constant.ApiConstant;
import org.innowise.userservice.model.dto.PaymentCardDto;
import org.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(ApiConstant.CARDS_BASE)
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardServiceImpl paymentCardServiceImpl;

    @PostMapping()
    public ResponseEntity<PaymentCardDto> create(@Valid @RequestBody PaymentCardDto paymentCardDto) {
        PaymentCardDto createdDto = paymentCardServiceImpl.create(paymentCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @GetMapping(ApiConstant.CARD_ID_PATH)
    public ResponseEntity<PaymentCardDto> getById(@PathVariable(name = "id") Long id) {
        PaymentCardDto paymentCardDto = paymentCardServiceImpl.getById(id);
        return ResponseEntity.ok(paymentCardDto);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAll(@RequestParam(name = "page", defaultValue = "0") int page,
                                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<PaymentCardDto> cards = paymentCardServiceImpl.getAll(page, size);
        return ResponseEntity.ok(cards);
    }

    @GetMapping(ApiConstant.CARDS_BY_USER_ID)
    public ResponseEntity<List<PaymentCardDto>> getAllByUserId(@PathVariable(name = "userId") Long userId) {
        List<PaymentCardDto> cards = paymentCardServiceImpl.getAllByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PostMapping(ApiConstant.CARD_ID_PATH)
    public ResponseEntity<PaymentCardDto> update(@PathVariable(name = "id") Long id,
                                                 @Valid @RequestBody PaymentCardDto paymentCardDto) {

        PaymentCardDto updatedCardDto = paymentCardServiceImpl.updateById(id, paymentCardDto);
        return ResponseEntity.ok(updatedCardDto);
    }

    @PostMapping(ApiConstant.ACTIVATE_CARD)
    public ResponseEntity<PaymentCardDto> activate(@PathVariable(name = "id") Long id) {
        PaymentCardDto activatedCard = paymentCardServiceImpl.activate(id);
        return ResponseEntity.ok(activatedCard);
    }

    @PostMapping(ApiConstant.DEACTIVATE_CARD)
    public ResponseEntity<PaymentCardDto> deactivate(@PathVariable(name = "id") Long id) {
        PaymentCardDto deactivatedCard = paymentCardServiceImpl.deactivate(id);
        return ResponseEntity.ok(deactivatedCard);
    }

}
