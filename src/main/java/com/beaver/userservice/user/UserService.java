package com.beaver.userservice.user;

import com.beaver.userservice.user.dto.UpdateSelf;
import com.beaver.userservice.user.mapper.IUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @CachePut(value = "users", key = "#user.email")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#email")
    public User getUserSelf(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @CachePut(value = "users", key = "#email")
    public User updateSelf(String email, UpdateSelf updateRequest) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.mapToEntity(updateRequest, existingUser);
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = "users", key = "#email")
    public void deleteUser(String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(existingUser);
    }
}
