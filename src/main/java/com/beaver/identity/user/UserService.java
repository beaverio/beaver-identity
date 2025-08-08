package com.beaver.identity.user;

import com.beaver.auth.jwt.AccessToken;
import com.beaver.auth.jwt.JwtService;
import com.beaver.identity.common.exception.NotFoundException;
import com.beaver.identity.common.exception.InvalidUserDataException;
import com.beaver.identity.workspace.dto.UpdateEmail;
import com.beaver.identity.workspace.dto.UpdatePassword;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.user.dto.UpdateSelf;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.user.mapper.IUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MembershipService membershipService;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'id:' + #id")
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Caching(put = {
            @CachePut(value = "users", key = "'id:' + #id"),
            @CachePut(value = "users", key = "'email:' + #result.email")
    })
    public User updateSelf(UUID id, UpdateSelf updateRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userMapper.mapToEntity(updateRequest, existingUser);
        return userRepository.save(existingUser);
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "'id:' + #id"),
        @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public void deleteUser(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userRepository.delete(existingUser);
    }

    @CacheEvict(value = "users", key = "'email:' + #oldEmail")
    public void evictOldEmailCache(String oldEmail) {
        // This method exists solely to evict the old email cache entry
    }

    @Caching(put = {
            @CachePut(value = "users", key = "'id:' + #result.id"),
            @CachePut(value = "users", key = "'email:' + #result.email")
    })
    public User createUser(String email, String password, String name) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new InvalidUserDataException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "'id:' + #id"),
            @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public User updateEmail(UUID id, UpdateEmail updateEmailRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userRepository.findByEmail(updateEmailRequest.email()).isPresent()) {
            throw new InvalidUserDataException("Email already exists");
        }

        String oldEmail = existingUser.getEmail();
        existingUser.setEmail(updateEmailRequest.email());
        User updatedUser = userRepository.save(existingUser);

        evictOldEmailCache(oldEmail);

        return updatedUser;
    }

    public String updateEmailWithNewToken(UUID userId, UUID workspaceId, UpdateEmail updateEmail) {
        User user = updateEmail(userId, updateEmail);

        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(userId);
        WorkspaceMembership currentMembership = memberships.stream()
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElse(memberships.getFirst());

        return jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(workspaceId.toString())
                        .role(currentMembership.getRole().toString())
                        .build()
        );
    }

    public void updatePassword(UUID userId, UpdatePassword updatePasswordRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(updatePasswordRequest.currentPassword(), existingUser.getPassword())) {
            throw new InvalidUserDataException("Invalid current password");
        }

        existingUser.setPassword(passwordEncoder.encode(updatePasswordRequest.newPassword()));
        userRepository.save(existingUser);
    }
}
