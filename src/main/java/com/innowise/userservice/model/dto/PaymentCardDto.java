package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotNull
  private Long userId;

  @NotNull
  private Long number;

  @NotNull
  @NotBlank
  private String holder;

  @NotNull
  @NotBlank
  private String expirationDate;

  private Boolean active;

  private String createdAt;

  private String updatedAt;
}
