package com.beaver.userservice.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private UserInfo user;
    private WorkspaceInfo workspace;

    @Data
    @Builder
    public static class UserInfo {
        private String id;
        private String email;
        private String name;
    }

    @Data
    @Builder
    public static class WorkspaceInfo {
        private String id;
        private String name;
    }
}
