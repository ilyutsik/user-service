package org.innowise.userservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.innowise.userservice.model.Auditable;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_cards",
        indexes = {
        @Index(name = "idx_card_info_user_id", columnList = "user_id"),
        @Index(name = "idx_card_info_expiration_date", columnList = "expiration_date")

        }
)
public class PaymentCard extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long number;

    @Column(nullable = false)
    private String holder;

    @Column(name = "expiration_date" ,nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private boolean active = true;

}
