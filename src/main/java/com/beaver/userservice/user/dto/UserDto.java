package com.beaver.userservice.user.dto;

import com.beaver.userservice.common.dto.BaseDto;
import com.beaver.userservice.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserDto extends BaseDto {

    private final String email;

    @JsonProperty("active")
    private final boolean isActive;

    private final String name;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.isActive())
                .name(user.getName())
                .build();
    }
}