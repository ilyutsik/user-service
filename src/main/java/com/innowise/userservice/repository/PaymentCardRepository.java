package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

  Long countByUserIdAndActiveTrue(Long userId);

  List<PaymentCard> findByUserIdAndActiveTrue(Long userId);

  @Query("SELECT c.user.id FROM PaymentCard c WHERE c.id = :cardId")
  Long findUserIdByCardId(@Param("cardId") Long cardId);
}
