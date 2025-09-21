package swd.billiardshop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd.billiardshop.entity.User;
import swd.billiardshop.dto.request.UserRegisterRequest;
import swd.billiardshop.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "role", expression = "java(swd.billiardshop.enums.Role.USER)")
    @Mapping(target = "status", expression = "java(swd.billiardshop.enums.Status.ACTIVE)")
    User toUser(UserRegisterRequest request);
}
