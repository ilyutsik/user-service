package com.innowise.userservice.model.entity;

import com.innowise.userservice.model.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_cards", indexes = {
    @Index(name = "idx_card_info_user_id", columnList = "user_id"),
    @Index(name = "idx_card_info_expiration_date", columnList = "expiration_date")})
public class PaymentCard extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 16)
  private String number;

  @Column(nullable = false, length = 50)
  private String holder;

  @Column(name = "expiration_date", nullable = false)
  private LocalDate expirationDate;

  @Column(nullable = false)
  private boolean active = true;
}
