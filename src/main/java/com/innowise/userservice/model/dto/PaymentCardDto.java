package com.innowise.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class PaymentCardDto {

  private Long id;

  private Long userId;

  @NotBlank(message = "Card number must not be blank")
  @Size(min = 16, max = 16, message = "Card number must contain exactly 16 digits")
  @Pattern(regexp = "\\d{16}", message = "Card number must contain digits only")
  private String number;

  @NotBlank(message = "Card holder must not be blank")
  @Size(min = 2, max = 50, message = "Card holder must be between 2 and 50 characters")
  private String holder;

  @NotNull(message = "Expiration date must not be null")
  @FutureOrPresent(message = "Expiration date must be in the present or future")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate expirationDate;

  private Boolean active;

  private LocalDate createdAt;

  private LocalDate updatedAt;
}