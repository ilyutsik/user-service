package org.innowise.userservice.repository;

import org.innowise.userservice.model.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    Long countByUserIdAndActiveTrue(Long userId);

    List<PaymentCard> findByUserIdAndActiveTrue(Long userId);
}
