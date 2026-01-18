package org.innowise.userservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.innowise.userservice.model.Auditable;
import org.innowise.userservice.model.entity.PaymentCard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users",
        indexes = {@Index(name = "idx_user_surname_name", columnList = "surname, name")})
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PaymentCard> cards = new ArrayList<>();

}
