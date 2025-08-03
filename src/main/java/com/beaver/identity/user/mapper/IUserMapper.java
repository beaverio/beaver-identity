package com.beaver.identity.user.mapper;

import com.beaver.identity.user.entity.User;
import com.beaver.identity.user.dto.UpdateSelf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IUserMapper {
    void mapToEntity(UpdateSelf dto, @MappingTarget User user);
}