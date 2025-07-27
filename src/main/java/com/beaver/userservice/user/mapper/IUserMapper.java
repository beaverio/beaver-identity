package com.beaver.userservice.user.mapper;

import com.beaver.userservice.user.User;
import com.beaver.userservice.user.dto.UpdateSelf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IUserMapper {
    void mapToEntity(UpdateSelf dto, @MappingTarget User user);
}