package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

  Long countByUserIdAndActiveTrue(Long userId);

  List<PaymentCard> findByUserIdAndActiveTrue(Long userId);
}
