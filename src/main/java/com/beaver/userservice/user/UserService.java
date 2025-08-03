package com.beaver.userservice.user;

import com.beaver.userservice.common.exception.UserNotFoundException;
import com.beaver.userservice.common.exception.InvalidUserDataException;
import com.beaver.userservice.user.dto.UpdateSelf;
import com.beaver.userservice.internal.dto.UpdateEmail;
import com.beaver.userservice.internal.dto.UpdatePassword;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.user.mapper.IUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "'id:' + #id")
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Caching(put = {
            @CachePut(value = "users", key = "'id:' + #user.id"),
            @CachePut(value = "users", key = "'email:' + #user.email")
    })
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Caching(put = {
            @CachePut(value = "users", key = "'id:' + #id"),
            @CachePut(value = "users", key = "'email:' + #result.email")
    })
    public User updateSelf(UUID id, UpdateSelf updateRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userMapper.mapToEntity(updateRequest, existingUser);
        return userRepository.save(existingUser);
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "'id:' + #id"),
        @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public User deleteUser(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(existingUser);
        return existingUser;
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "'id:' + #id"),
        @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public User updateEmail(UUID id, UpdateEmail updateEmailRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(updateEmailRequest.currentPassword(), existingUser.getPassword())) {
            throw new InvalidUserDataException("Invalid current password");
        }

        if (userRepository.findByEmail(updateEmailRequest.email()).isPresent()) {
            throw new InvalidUserDataException("Email already exists");
        }

        String oldEmail = existingUser.getEmail();
        existingUser.setEmail(updateEmailRequest.email());
        User updatedUser = userRepository.save(existingUser);

        evictOldEmailCache(oldEmail);

        return updatedUser;
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "'id:' + #id"),
        @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public User updatePassword(UUID id, UpdatePassword updatePasswordRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(updatePasswordRequest.currentPassword(), existingUser.getPassword())) {
            throw new InvalidUserDataException("Invalid current password");
        }

        existingUser.setPassword(passwordEncoder.encode(updatePasswordRequest.newPassword()));
        return userRepository.save(existingUser);
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
                .
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
