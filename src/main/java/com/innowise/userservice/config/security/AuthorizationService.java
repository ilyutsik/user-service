package com.innowise.userservice.config.security;

import com.innowise.userservice.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  private final PaymentCardRepository cardRepository;

  public boolean isOwner(Long id) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().equals(id.toString());
  }

  public boolean isCardOwner(Long cardId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Long currentUserId = Long.parseLong(auth.getName());

    Long ownerId = cardRepository.findUserIdByCardId(cardId);
    return ownerId != null && ownerId.equals(currentUserId);
  }
}