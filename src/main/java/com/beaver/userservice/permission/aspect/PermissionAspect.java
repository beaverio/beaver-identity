package com.beaver.userservice.permission.aspect;

import com.beaver.userservice.permission.PermissionService;
import com.beaver.userservice.permission.annotation.RequiresPermission;
import com.beaver.userservice.permission.enums.Permission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final PermissionService permissionService;

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String workspaceId = request.getHeader("X-Workspace-Id");
        String userId = request.getHeader("X-User-Id");
        String permissionsHeader = request.getHeader("X-User-Permissions");

        if (workspaceId == null || userId == null) {
            throw new AccessDeniedException("Missing workspace or user context");
        }

        Set<String> userPermissions = permissionsHeader != null ?
                Set.of(permissionsHeader.split(",")) : Set.of();

        Permission[] requiredPermissions = requiresPermission.value();
        boolean requireAll = requiresPermission.requireAll();

        boolean hasAccess = requireAll ?
                Arrays.stream(requiredPermissions).allMatch(perm ->
                        permissionService.hasPermission(userPermissions, perm.getValue())) :
                Arrays.stream(requiredPermissions).anyMatch(perm ->
                        permissionService.hasPermission(userPermissions, perm.getValue()));

        if (!hasAccess) {
            String permissionNames = Arrays.stream(requiredPermissions)
                    .map(Permission::name)
                    .collect(Collectors.joining(", "));
            log.warn("Access denied for user {} in workspace {}. Required permissions: {}",
                    userId, workspaceId, permissionNames);
            throw new AccessDeniedException("Insufficient permissions: " + permissionNames);
        }

        return joinPoint.proceed();
    }
}
