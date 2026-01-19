package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "expirationDate", source = "expirationDate", qualifiedByName = "localDateToString")
  PaymentCardDto toDto(PaymentCard paymentCard);

  @Mapping(target = "user", source = "userId")
  @Mapping(target = "expirationDate", source = "expirationDate", qualifiedByName = "stringToLocalDate")
  PaymentCard toEntity(PaymentCardDto paymentCardDto);

  default User map(Long id) {
    if (id == null) {
      return null;
    }
    User user = new User();
    user.setId(id);
    return user;
  }

  @Named("localDateToString")
  default String localDateToString(LocalDate date) {
    return date != null ? date.toString() : null;
  }

  @Named("stringToLocalDate")
  default LocalDate stringToLocalDate(String date) {
    return date != null ? LocalDate.parse(date) : null;
  }
}
