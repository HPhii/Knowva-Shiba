package com.example.demo.mapper;

import com.example.demo.model.entity.Account;
import com.example.demo.model.io.response.object.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "token", ignore = true)
    AccountResponse toAccountResponse(Account account);
}
