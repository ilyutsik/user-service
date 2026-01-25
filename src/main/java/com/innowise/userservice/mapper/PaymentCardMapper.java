package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

  @Mapping(target = "userId", source = "user.id")
  PaymentCardDto toDto(PaymentCard paymentCard);

  @Mapping(target = "user", source = "userId")
  PaymentCard toEntity(PaymentCardDto paymentCardDto);

  default User map(Long id) {
    if (id == null) {
      return null;
    }
    User user = new User();
    user.setId(id);
    return user;
  }
}
