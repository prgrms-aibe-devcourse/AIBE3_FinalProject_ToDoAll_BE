package com.server.admin.service;

import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void softDelete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.softDelete();
    }

    public void restore(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.restore();
    }
}
