package com.beaver.identity.role.entity;

import com.beaver.auth.roles.Role;
import com.beaver.identity.common.entity.BaseEntity;
import com.beaver.identity.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@DynamicUpdate
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "role_type"})
})
public class WorkspaceRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "role_type", nullable = false, length = 10)
    @Convert(converter = RoleConverter.class)
    private Role roleType;

    public String getName() {
        return roleType.name();
    }

    @Override
    public String toString() {
        return roleType.name();
    }

    @jakarta.persistence.Converter
    public static class RoleConverter implements AttributeConverter<Role, String> {
        @Override
        public String convertToDatabaseColumn(Role role) {
            return role != null ? role.name() : null;
        }

        @Override
        public Role convertToEntityAttribute(String dbData) {
            if (dbData == null) return null;
            return Role.valueOf(dbData);
        }
    }
}
