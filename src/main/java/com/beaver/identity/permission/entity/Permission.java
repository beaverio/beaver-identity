package com.beaver.identity.permission.entity;

import com.beaver.identity.common.entity.BaseEntity;
import com.beaver.identity.permission.enums.PermissionCategory;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, length = 50)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionCategory category;
}