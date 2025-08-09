package com.beaver.identity.user;

import com.beaver.auth.jwt.AccessToken;
import com.beaver.auth.jwt.JwtService;
import com.beaver.identity.common.exception.NotFoundException;
import com.beaver.identity.common.exception.InvalidUserDataException;
import com.beaver.identity.common.mapper.GenericMapper;
import com.beaver.identity.user.dto.UpdateEmail;
import com.beaver.identity.user.dto.UpdatePassword;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.user.dto.UpdateUser;
import com.beaver.identity.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
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

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MembershipService membershipService;
    private final GenericMapper mapper;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    @Cacheable(key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "'id:' + #id")
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Caching(put = {
            @CachePut(key = "'id:' + #id"),
            @CachePut(key = "'email:' + #result.email")
    })
    public User updateUser(UUID id, UpdateUser updateRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        mapper.updateEntity(updateRequest, existingUser);

        return userRepository.save(existingUser);
    }

    @Caching(evict = {
        @CacheEvict(key = "'id:' + #id"),
        @CacheEvict(key = "'email:' + #result.email")
    })
    public User deleteUser(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userRepository.delete(existingUser);
        return existingUser;
    }

    @Caching(put = {
            @CachePut(key = "'id:' + #result.id"),
            @CachePut(key = "'email:' + #result.email")
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
            @CacheEvict(key = "'id:' + #id"),
            @CacheEvict(key = "'email:' + #result.email")
    })
    public User updateEmail(UUID id, UpdateEmail updateEmailRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userRepository.findByEmail(updateEmailRequest.email()).isPresent()) {
            throw new InvalidUserDataException("Email already exists");
        }

        String oldEmail = existingUser.getEmail();
        var cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evictIfPresent("email:" + oldEmail);
        }

        existingUser.setEmail(updateEmailRequest.email());
        return userRepository.save(existingUser);
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
