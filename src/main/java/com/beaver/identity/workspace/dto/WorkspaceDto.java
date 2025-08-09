package com.beaver.identity.workspace.dto;

import com.beaver.identity.common.dto.BaseDto;
import com.beaver.identity.workspace.enums.PlanType;
import com.beaver.identity.workspace.entity.Workspace;
import com.beaver.identity.workspace.enums.WorkspaceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WorkspaceDto extends BaseDto {
    private String name;
    private WorkspaceStatus status;
    private PlanType plan;
    private LocalDateTime trialEndsAt;
}
