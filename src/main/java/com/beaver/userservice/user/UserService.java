package com.beaver.userservice.user;

import com.beaver.userservice.common.exception.UserNotFoundException;
import com.beaver.userservice.user.dto.UpdateSelf;
import com.beaver.userservice.user.mapper.IUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#id")
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @CachePut(value = "users", key = "#user.email")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @CachePut(value = "users", key = "#email")
    public User updateSelf(String email, UpdateSelf updateRequest) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.mapToEntity(updateRequest, existingUser);
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(existingUser);
    }
}
