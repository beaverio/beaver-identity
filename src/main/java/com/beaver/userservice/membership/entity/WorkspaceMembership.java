package com.beaver.userservice.membership.entity;

import com.beaver.userservice.common.entity.BaseEntity;
import com.beaver.userservice.membership.enums.MembershipStatus;
import com.beaver.userservice.permission.entity.Permission;
import com.beaver.userservice.workspace.entity.Workspace;
import com.beaver.userservice.permission.entity.Role;
import com.beaver.userservice.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DynamicUpdate
@Entity
@Table(name = "workspace_memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "workspace_id"})
})
public class WorkspaceMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public Set<String> getAllPermissionCodes() {
        return role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public boolean isOwner() {
        return "Owner".equals(role.getName());
    }

    public boolean isViewer() {
        return "Viewer".equals(role.getName());
    }
}